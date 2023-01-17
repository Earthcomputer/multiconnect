package net.earthcomputer.multiconnect.protocols.generic;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class AssetDownloader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();

    private static final Pattern ARG_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d.]*[dfs]");
    private static final Pattern LANG_ASSET_PATTERN = Pattern.compile("minecraft/lang/([a-zA-Z_]+)\\.(json|lang)");

    private static final URL VERSION_MANIFEST = FileDownloader.createURL("https://launchermeta.mojang.com/mc/game/version_manifest.json");
    private static final String ASSET_URL_FORMAT = "https://resources.download.minecraft.net/%s/%s";

    private static Map<String, String> versionUrls;
    private static final Map<String, String> versionAssetUrls = new HashMap<>();
    private static final Map<String, Map<String, String>> langFileUrls = new HashMap<>();
    private static final Map<String, Map<String, Path>> langFiles = new HashMap<>();

    private AssetDownloader() {
    }

    public static void reloadLanguages() {
        Minecraft.getInstance().getLanguageManager().onResourceManagerReload(Minecraft.getInstance().getResourceManager());
        if (FabricLoader.getInstance().isModLoaded("optifabric")) {
            try {
                Class.forName("net.optifine.Lang").getMethod("resourcesReloaded").invoke(null);
            } catch (ReflectiveOperationException e) {
                LOGGER.error("Failed to force OptiFine to reload language files!", e);
            }
        }
    }

    public static void addExtraTranslations(String nativeLang, BiConsumer<String, String> translations) {
        if (ConnectionInfo.protocol == ProtocolRegistry.latestBehaviorSet()) {
            return;
        }

        String currentVersion = ProtocolRegistry.getName(ConnectionInfo.protocolVersion);
        String latestVersion = ProtocolRegistry.getName(SharedConstants.getCurrentVersion().getProtocolVersion());

        Map<String, String> currentNative = getTranslations(currentVersion, nativeLang);
        Map<String, String> currentFallback = getTranslations(currentVersion, "en_us");
        Map<String, String> latestNative = getTranslations(latestVersion, nativeLang);
        Map<String, String> latestFallback = getTranslations(latestVersion, "en_us");

        for (String key : currentNative.keySet()) {
            if (!latestNative.containsKey(key) || argCount(currentNative.get(key)) != argCount(latestNative.get(key))) {
                translations.accept(key, currentNative.get(key));
            }
        }
        for (String key : currentFallback.keySet()) {
            if (!currentNative.containsKey(key)) {
                if (!latestFallback.containsKey(key) || argCount(currentFallback.get(key)) != argCount(latestFallback.get(key))) {
                    translations.accept(key, currentFallback.get(key));
                }
            }
        }
    }

    private static int argCount(String value) {
        Matcher matcher = ARG_PATTERN.matcher(value);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private static Map<String, String> getVersionUrls() {
        if (versionUrls != null) {
            return versionUrls;
        }

        VersionManifest manifest = FileDownloader.downloadJson(VERSION_MANIFEST, "version_manifest.json", VersionManifest.class);
        if (manifest == null) {
            return versionUrls = Collections.emptyMap();
        }

        versionUrls = new HashMap<>();
        for (VersionManifest.Version version : manifest.versions) {
            if (ProtocolRegistry.isSupportedName(version.id)) {
                versionUrls.put(version.id, version.url);
            }
        }
        return versionUrls;
    }

    private static String getAssetUrl(String version) {
        if (versionAssetUrls.containsKey(version)) {
            return versionAssetUrls.get(version);
        }
        VersionFile versionFile = getVersionFile(version);
        if (versionFile == null) {
            versionAssetUrls.put(version, null);
            return null;
        }

        versionAssetUrls.put(version, versionFile.assetIndex.url);
        return versionFile.assetIndex.url;
    }

    private static VersionFile getVersionFile(String version) {
        URL versionUrl;
        try {
            String url = getVersionUrls().get(version);
            if (url == null) {
                LOGGER.error("No version URL found for version {}", version);
                versionAssetUrls.put(version, null);
                return null;
            }
            versionUrl = new URL(url);
        } catch (MalformedURLException e) {
            LOGGER.error("Malformed version URL for version {}", version);
            versionAssetUrls.put(version, null);
            return null;
        }

        return FileDownloader.downloadJson(versionUrl, version + "/" + version + ".json", VersionFile.class);
    }

    private static Map<String, String> getLangFileUrls(String version) {
        if (langFileUrls.containsKey(version)) {
            return langFileUrls.get(version);
        }

        URL assetUrl;
        try {
            String url = getAssetUrl(version);
            if (url == null) {
                LOGGER.error("No asset URL found for version {}", version);
                langFileUrls.put(version, Collections.emptyMap());
                return Collections.emptyMap();
            }
            assetUrl = new URL(url);
        } catch (MalformedURLException e) {
            LOGGER.error("Malformed asset URL for version {}", version);
            langFileUrls.put(version, Collections.emptyMap());
            return Collections.emptyMap();
        }

        AssetFile assets = FileDownloader.downloadJson(assetUrl, version + "/indexes.json", AssetFile.class);
        if (assets == null) {
            langFileUrls.put(version, Collections.emptyMap());
            return Collections.emptyMap();
        }

        Map<String, String> urls = new HashMap<>();
        assets.objects.forEach((k, v) -> {
            Matcher matcher = LANG_ASSET_PATTERN.matcher(k);
            if (matcher.matches() && v.hash.length() >= 2) {
                urls.put(matcher.group(1).toLowerCase(Locale.ENGLISH), String.format(ASSET_URL_FORMAT, v.hash.substring(0, 2), v.hash));
            }
        });

        langFileUrls.put(version, urls);
        return urls;
    }

    private static Path getLangFile(String version, String langCode) {
        langCode = langCode.toLowerCase(Locale.ENGLISH);
        // en_us is in the jar file rather than in the assets, we don't want to have to download the whole jar
        if ("en_us".equals(langCode)) {
            langCode = "en_gb";
        }

        if (langFiles.computeIfAbsent(version, k -> new HashMap<>()).containsKey(langCode)) {
            return langFiles.get(version).get(langCode);
        }

        URL langUrl;
        try {
            String url = getLangFileUrls(version).get(langCode);
            if (url == null) {
                langFiles.get(version).put(langCode, null);
                return null;
            }
            langUrl = new URL(url);
        } catch (MalformedURLException e) {
            LOGGER.error("Malformed lang url for {}/{}", version, langCode);
            langFiles.get(version).put(langCode, null);
            return null;
        }

        Path langFile = FileDownloader.download(langUrl, version + "/" + langCode + ".lang");
        langFiles.get(version).put(langCode, langFile);
        return langFile;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> getTranslations(String version, String langCode) {
        Path langFile = getLangFile(version, langCode);
        if (langFile == null) {
            return Collections.emptyMap();
        }

        try {
            Map<String, Object> translations;
            try (BufferedReader reader = Files.newBufferedReader(langFile)) {
                translations = GSON.fromJson(reader, Map.class);
            }
            translations.keySet().removeIf(k -> !(translations.get(k) instanceof String));
            return (Map<String, String>) (Object) translations;
        } catch (JsonSyntaxException e) {
            // expected - old lang file format
        } catch (IOException e) {
            LOGGER.error("Failed to read lang file {}/{}", version, langCode, e);
            return Collections.emptyMap();
        }

        Map<String, String> translations = new HashMap<>();
        try (Stream<String> lines = Files.lines(langFile)) {
            lines.forEach(line -> {
                if (line.contains("=") && !line.startsWith("#")) {
                    String[] parts = line.split("=", 2);
                    translations.put(parts[0], parts[1]);
                }
            });
        } catch (IOException e) {
            LOGGER.error("Failed to read lang file in old format {}/{}", version, langCode, e);
            return Collections.emptyMap();
        }
        return translations;
    }

    public static Path downloadServer(String version) {
        VersionFile versionFile = getVersionFile(version);
        if (versionFile == null) {
            return null;
        }
        URL serverUrl;
        try {
            serverUrl = new URL(versionFile.downloads.server.url);
        } catch (MalformedURLException e) {
            LOGGER.error("Malformed url for server version {}", version, e);
            return null;
        }
        return FileDownloader.downloadAndParse(serverUrl, version + "/server.jar", path -> {
            try (JarFile ignored = new JarFile(path.toFile())) {
                return path;
            }
        });
    }

    static class VersionManifest {
        List<Version> versions;
        static class Version {
            String id;
            String url;
        }
    }

    static class VersionFile {
        Asset assetIndex;
        Downloads downloads;
    }

    static class Downloads {
        Asset server;
    }

    static class AssetFile {
        Map<String, Obj> objects;
        static class Obj {
            String hash;
        }
    }

    static class Asset {
        String url;
    }
}
