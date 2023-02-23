package net.earthcomputer.multiconnect.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import net.earthcomputer.multiconnect.api.IMulticonnectTranslator;
import net.earthcomputer.multiconnect.api.IMulticonnectTranslatorApi;
import net.earthcomputer.multiconnect.protocols.generic.FileDownloader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class TranslatorDiscoverer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static final Pattern GIT_DESCRIBE_PATTERN = Pattern.compile("-[0-9a-z]([0-9a-z]{8})$");

    public static IMulticonnectTranslator discoverTranslator(IMulticonnectTranslatorApi api) {
        maybeDownloadTranslators();

        ServiceLoader<IMulticonnectTranslator> loader = ServiceLoader.load(IMulticonnectTranslator.class);
        return loader.stream()
            .map(ServiceLoader.Provider::get)
            .filter(translator -> translator.isApplicableInEnvironment(api))
            .max(Comparator.comparingInt(IMulticonnectTranslator::priority))
            .orElseThrow();
    }

    private static void maybeDownloadTranslators() {
        if (FabricLoader.getInstance().isDevelopmentEnvironment() && !Boolean.getBoolean("multiconnect.translatorDiscoveryInDev")) {
            LOGGER.info("Skipping translator discovery due to development environment");
            return;
        }

        if (loadCustomTranslator("multiconnect.customTranslator")) {
            return;
        }

        if (Boolean.getBoolean("multiconnect.skipTranslatorDiscovery")) {
            LOGGER.info("Skipping translator discovery due to system property");
            return;
        }

        if (!FabricLoader.getInstance().isModLoaded("viafabric")) {
            CompletableFuture<Void> viaVersionFuture = CompletableFuture.runAsync(TranslatorDiscoverer::maybeDownloadViaVersion);
            if (!loadCustomTranslator("multiconnect.customMulticonnectViaTranslator")) {
                CompletableFuture<Void> viaTranslatorFuture = CompletableFuture.runAsync(TranslatorDiscoverer::maybeDownloadViaTranslator);
                viaTranslatorFuture.join();
            }
            viaVersionFuture.join();
        }
    }

    private static boolean loadCustomTranslator(String systemProp) {
        String customTranslator = System.getProperty(systemProp);
        if (customTranslator != null) {
            boolean success = false;
            try {
                Path file = Path.of(customTranslator);
                if (Files.exists(file)) {
                    success = true;
                    addToClassPath(file);
                }
            } catch (InvalidPathException e) {
                success = false;
            }
            if (!success) {
                LOGGER.error("Failed to load custom translator {}, falling back to translator discovery", customTranslator);
            } else {
                return true;
            }
        }
        return false;
    }

    private static void maybeDownloadViaTranslator() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("multiconnect");
        if (Files.exists(configDir)) {
            try (Stream<Path> dir = Files.list(configDir)) {
                for (Path file : (Iterable<Path>) dir::iterator) {
                    if (isMatchingViaTranslatorJar(file)) {
                        LOGGER.info("Found via translator: {}", file);
                        addToClassPath(file);
                        return;
                    }
                }
            } catch (IOException e) {
                LOGGER.error("An I/O error occurred listing files in directory", e);
            }
        }

        downloadViaTranslator(configDir);
    }

    private static boolean isMatchingViaTranslatorJar(Path file) {
        String fileName = file.getFileName().toString();
        if (!fileName.startsWith("via-translator") || !fileName.endsWith(".jar")) {
            return false;
        }

        try (JarFile jar = new JarFile(file.toFile())) {
            JarEntry modJsonEntry = jar.getJarEntry("fabric.mod.json");
            if (modJsonEntry == null) {
                return false;
            }

            JsonObject modJson = GSON.fromJson(new InputStreamReader(jar.getInputStream(modJsonEntry), StandardCharsets.UTF_8), JsonObject.class);
            if (modJson == null) {
                return false;
            }

            String version = GsonHelper.getAsString(modJson, "version", "");
            if (!"${version}".equals(version) && !Multiconnect.getVersion().equals(version)) {
                return false;
            }
        } catch (IOException | JsonSyntaxException e) {
            LOGGER.error("Failed to open via translator jar file {}", file, e);
            return false;
        }

        return true;
    }

    private static void downloadViaTranslator(Path configDir) {
        Matcher matcher = GIT_DESCRIBE_PATTERN.matcher(Multiconnect.getVersion());
        if (matcher.find()) {
            downloadViaTranslatorFromGithubActions(matcher.group(1));
        } else {
            downloadViaTranslatorFromModrinth(configDir);
        }
    }

    private static void downloadViaTranslatorFromModrinth(Path configDir) {
        URI versionsUri;
        try {
            versionsUri = new URI("https://api.modrinth.com/v2/project/MNhf9veJ/version");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        try {
            HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();

            String userAgent = "multiconnect " + Multiconnect.getVersion();

            HttpRequest versionsRequest = HttpRequest.newBuilder().uri(versionsUri).header("User-Agent", userAgent).build();
            HttpResponse<InputStream> versionsResponse = client.send(versionsRequest, HttpResponse.BodyHandlers.ofInputStream());
            JsonArray versions = GSON.fromJson(new InputStreamReader(versionsResponse.body(), StandardCharsets.UTF_8), JsonArray.class);
            if (versions == null) {
                throw new JsonSyntaxException("versions is null");
            }
            JsonArray versionFiles = null;
            for (JsonElement version : versions) {
                JsonObject versionObj = GsonHelper.convertToJsonObject(version, "version");
                String versionStr = GsonHelper.getAsString(versionObj, "version_number");
                if (versionStr.equals(Multiconnect.getVersion())) {
                    versionFiles = GsonHelper.getAsJsonArray(versionObj, "files");
                    break;
                }
            }

            if (versionFiles == null) {
                LOGGER.error("Could not find multiconnect via translator version for current multiconnect version {}", Multiconnect.getVersion());
                return;
            }

            String fileUrl = null;
            for (JsonElement file : versionFiles) {
                JsonObject fileObj = GsonHelper.convertToJsonObject(file, "file");
                if (GsonHelper.getAsBoolean(fileObj, "primary", false)) {
                    fileUrl = GsonHelper.getAsString(fileObj, "url");
                    break;
                }
            }

            if (fileUrl == null) {
                LOGGER.error("Could not find multiconnect via translator version for current multiconnect version {}", Multiconnect.getVersion());
                return;
            }

            LOGGER.info("Found multiconnect via translator download URL: {}", fileUrl);

            URI fileUri;
            try {
                fileUri = new URI(fileUrl);
            } catch (URISyntaxException e) {
                LOGGER.error("Invalid download URL: {}", fileUrl);
                return;
            }
            HttpRequest downloadRequest = HttpRequest.newBuilder().uri(fileUri).header("User-Agent", userAgent).build();
            Path downloadedFile = configDir.resolve("via-translator-" + Multiconnect.getVersion() + ".jar");
            HttpResponse<Path> downloadResponse = client.send(downloadRequest, HttpResponse.BodyHandlers.ofFile(downloadedFile));
            addToClassPath(downloadResponse.body());
            LOGGER.info("Downloaded multiconnect via translator");
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted while downloading multiconnect via translator", e);
        } catch (UnknownHostException e) {
            LOGGER.error("Couldn't connect to modrinth, check your internet connection", e);
        } catch (IOException | JsonSyntaxException e) {
            LOGGER.error("Unable to download multiconnect via translator", e);
        }
    }

    private static void downloadViaTranslatorFromGithubActions(String gitHashStart) {
        JsonObject runs = FileDownloader.downloadJson(
            FileDownloader.createURL("https://api.github.com/repos/Earthcomputer/multiconnect/actions/runs"),
            "multiconnect_runs.json",
            JsonObject.class
        );
        if (runs == null) {
            return;
        }

        String downloadUrl = null;
        String fileName = null;

        runLoop:
        for (JsonElement runElement : GsonHelper.getAsJsonArray(runs, "workflow_runs")) {
            JsonObject run = GsonHelper.convertToJsonObject(runElement, "workflow run");
            if (!".github/workflows/build.yml".equals(GsonHelper.getAsString(run, "path"))) {
                continue;
            }
            if (!GsonHelper.getAsString(run, "head_sha").startsWith(gitHashStart)) {
                continue;
            }

            URL artifactsUrl;
            try {
                artifactsUrl = new URL(GsonHelper.getAsString(run, "artifacts_url"));
            } catch (MalformedURLException e) {
                LOGGER.error("GitHub gave malformed artifacts URL: {}", GsonHelper.getAsString(run, "artifacts_url"));
                continue;
            }
            long checkSuiteId = GsonHelper.getAsLong(run, "check_suite_id");
            JsonObject artifacts = FileDownloader.downloadJson(
                artifactsUrl,
                "multiconnect_run_" + GsonHelper.getAsLong(run, "id") + "_artifacts.json",
                JsonObject.class
            );
            if (artifacts == null) {
                continue;
            }
            for (JsonElement artifactElement : GsonHelper.getAsJsonArray(artifacts, "artifacts")) {
                JsonObject artifact = GsonHelper.convertToJsonObject(artifactElement, "artifact");
                if (!"via-translator-snapshot".equals(GsonHelper.getAsString(artifact, "name"))) {
                    continue;
                }
                long artifactId = GsonHelper.getAsLong(artifact, "id");
                if (GsonHelper.getAsBoolean(artifact, "expired", false)) {
                    LOGGER.warn("Found a matching via-translator artifact (ID {}) on GitHub actions, but it has expired. Please compile manually.", artifactId);
                    continue;
                }
                downloadUrl = "https://nightly.link/Earthcomputer/multiconnect/suites/" + checkSuiteId + "/artifacts/" + artifactId;
                fileName = "multiconnect_" + checkSuiteId + "_" + artifactId + ".jar";
                break runLoop;
            }
        }

        if (downloadUrl == null) {
            LOGGER.warn("Could not find a matching via-translator artifact. Please compile manually.");
            return;
        }

        Path viaTranslatorJar = FileDownloader.downloadAndParse(
            FileDownloader.createURL(downloadUrl),
            fileName,
            (in, dest) -> {
                ZipInputStream zis = new ZipInputStream(in);
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().endsWith(".jar") && !entry.getName().contains("sources")) {
                        Files.copy(zis, dest, StandardCopyOption.REPLACE_EXISTING);
                        return;
                    }
                }
                throw new IOException("Could not find via-translator.jar in the zip");
            },
            file -> {
                try (JarFile ignored = new JarFile(file.toFile())) {
                    return file;
                }
            }
        );

        if (viaTranslatorJar != null) {
            addToClassPath(viaTranslatorJar);
            LOGGER.info("Downloaded multiconnect via translator");
        }
    }

    private static void maybeDownloadViaVersion() {
        Path viaVersionJar = FileDownloader.downloadAndParse(
            FileDownloader.createURL("https://ci.viaversion.com/job/ViaVersion/lastSuccessfulBuild/artifact/*zip*/target.zip"),
            "viaversion.jar",
            (in, dest) -> {
                ZipInputStream zis = new ZipInputStream(in);
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().endsWith(".jar")) {
                        Files.copy(zis, dest, StandardCopyOption.REPLACE_EXISTING);
                        return;
                    }
                }
                throw new IOException("Could not find viaversion.jar in the zip");
            },
            file -> {
                try (JarFile ignored = new JarFile(file.toFile())) {
                    return file;
                }
            });

        if (viaVersionJar != null) {
            addToClassPath(viaVersionJar);
            LOGGER.info("Downloaded ViaVersion");
        }
    }

    private static synchronized void addToClassPath(Path path) {
        try {
            Class<?> launcherBase;
            try {
                launcherBase = Class.forName("net.fabricmc.loader.impl.launch.FabricLauncherBase");
            } catch (ClassNotFoundException e) {
                launcherBase = Class.forName("org.quiltmc.loader.impl.launch.QuiltLauncherBase");
            }
            Method getLauncher = launcherBase.getMethod("getLauncher");
            Object launcher = getLauncher.invoke(null);
            getLauncher.getReturnType().getMethod("addToClassPath", Path.class, String[].class).invoke(launcher, path, new String[0]);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to add multiconnect via translator to classpath. Report this to https://github.com/Earthcomputer/multiconnect/issues", e);
        }

        try (JarInputStream jar = new JarInputStream(Files.newInputStream(path))) {
            JarEntry entry;
            while ((entry = jar.getNextJarEntry()) != null) {
                String name = entry.getName();
                if (name.startsWith("META-INF/jars/") && name.endsWith(".jar")) {
                    Path destJar = Files.createTempFile(null, ".jar");
                    destJar.toFile().deleteOnExit();
                    Files.copy(jar, destJar, StandardCopyOption.REPLACE_EXISTING);
                    addToClassPath(destJar);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read jar file {}", path, e);
        }
    }
}
