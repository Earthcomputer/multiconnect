package net.earthcomputer.multiconnect.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import net.earthcomputer.multiconnect.api.IMulticonnectTranslator;
import net.earthcomputer.multiconnect.api.IMulticonnectTranslatorApi;
import net.earthcomputer.multiconnect.protocols.generic.AssetDownloader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public final class TranslatorDiscoverer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();

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
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            LOGGER.info("Skipping translator discovery due to development environment");
            return;
        }
        if (Boolean.getBoolean("multiconnect.skipTranslatorDiscovery")) {
            LOGGER.info("Skipping translator discovery due to system property");
            return;
        }

        if (!FabricLoader.getInstance().isModLoaded("viafabric")) {
            CompletableFuture<Void> viaTranslatorFuture = CompletableFuture.runAsync(TranslatorDiscoverer::maybeDownloadViaTranslator);
            CompletableFuture<Void> viaVersionFuture = CompletableFuture.runAsync(TranslatorDiscoverer::maybeDownloadViaVersion);
            viaTranslatorFuture.join();
            viaVersionFuture.join();
        }
    }

    private static void maybeDownloadViaTranslator() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("multiconnect");
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
        URI versionsUri;
        try {
            versionsUri = new URI("https://api.modrinth.com/v2/project/MNhf9veJ/version");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        try {
            HttpClient client = HttpClient.newHttpClient();

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

    private static void maybeDownloadViaVersion() {
        URL viaMavenMetadataUrl;
        try {
            viaMavenMetadataUrl = new URL("https://repo.viaversion.com/com/viaversion/viaversion/maven-metadata.xml");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        Document metadata = AssetDownloader.downloadXml(viaMavenMetadataUrl, "via-maven-metadata.xml");
        if (metadata == null) {
            return;
        }
        Element versioning = getChildElement(metadata.getDocumentElement(), "versioning");
        if (versioning == null) {
            LOGGER.info("Malformed via maven metadata");
            return;
        }
        Element latest = getChildElement(versioning, "latest");
        if (latest == null) {
            LOGGER.info("Malformed via maven metadata");
            return;
        }
        String latestVersion = latest.getTextContent();
        LOGGER.info("Latest ViaVersion is {}", latestVersion);

        String viaVersionUrlStr = "https://repo.viaversion.com/com/viaversion/viaversion/" + latestVersion + "/viaversion-" + latestVersion + ".jar";
        URL viaVersionUrl;
        try {
            viaVersionUrl = new URL(viaVersionUrlStr);
        } catch (MalformedURLException e) {
            LOGGER.error("Malformed URL: {}", viaVersionUrlStr);
            return;
        }
        File viaVersionJar = AssetDownloader.downloadJar(viaVersionUrl, "viaversion-" + latestVersion + ".jar");
        if (viaVersionJar == null) {
            return;
        }

        addToClassPath(viaVersionJar.toPath());
        LOGGER.info("ViaVersion downloaded");
    }

    private static Element getChildElement(Element element, String name) {
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && name.equals(child.getNodeName())) {
                return (Element) child;
            }
        }
        return null;
    }

    private static void addToClassPath(Path path) {
        try {
            Class<?> launcherBase;
            try {
                launcherBase = Class.forName("net.fabricmc.loader.impl.launch.FabricLauncherBase");
            } catch (ClassNotFoundException e) {
                launcherBase = Class.forName("org.quiltmc.loader.impl.launch.QuiltLauncherBase");
            }
            Object launcher = launcherBase.getMethod("getLauncher").invoke(null);
            launcher.getClass().getMethod("addToClassPath", Path.class).invoke(launcher, path);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to add multiconnect via translator to classpath. Report this to https://github.com/Earthcomputer/multiconnect/issues", e);
        }
    }
}
