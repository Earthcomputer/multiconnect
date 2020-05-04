package net.earthcomputer.multiconnect.impl;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OldLanguageManager {

    private static final Logger LOGGER = LogManager.getLogger("multiconnect");
    private static final Gson GSON = new Gson();

    private static final Pattern ARG_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d.]*[dfs]");
    private static final Pattern LANG_ASSET_PATTERN = Pattern.compile("minecraft/lang/([a-zA-Z_]+)\\.(json|lang)");
    private static final File CACHE_DIR = new File(FabricLoader.getInstance().getConfigDirectory(), "multiconnect/caches");
    private static final URL VERSION_MANIFEST = createURL("https://launchermeta.mojang.com/mc/game/version_manifest.json");
    private static final String ASSET_URL_FORMAT = "https://resources.download.minecraft.net/%s/%s";

    private static Map<String, String> versionUrls;
    private static Map<String, String> versionAssetUrls = new HashMap<>();
    private static Map<String, Map<String, String>> langFileUrls = new HashMap<>();
    private static Map<String, Map<String, File>> langFiles = new HashMap<>();

    public static void reloadLanguages() {
        MinecraftClient.getInstance().getLanguageManager().apply(MinecraftClient.getInstance().getResourceManager());
        if (FabricLoader.getInstance().isModLoaded("optifabric")) {
            try {
                Class.forName("net.optifine.Lang").getMethod("resourcesReloaded").invoke(null);
            } catch (ReflectiveOperationException e) {
                LOGGER.error("Failed to force OptiFine to reload language files!", e);
            }
        }
    }

    private static URL createURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    public static void addExtraTranslations(String nativeLang, BiConsumer<String, String> translations) {
        if (ConnectionInfo.protocol == ProtocolRegistry.latest())
            return;

        String currentVersion = ConnectionMode.byValue(ConnectionInfo.protocolVersion).getAssetId();
        String latestVersion = ConnectionMode.byValue(SharedConstants.getGameVersion().getProtocolVersion()).getAssetId();

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
        while (matcher.find())
            count++;
        return count;
    }

    private static Map<String, String> getVersionUrls() {
        if (versionUrls != null)
            return versionUrls;

        File versionsFile = download(VERSION_MANIFEST, "version_manifest.json");
        if (versionsFile == null) {
            return versionUrls = Collections.emptyMap();
        }

        VersionManifest manifest;
        try {
            manifest = GSON.fromJson(new FileReader(versionsFile), VersionManifest.class);
        } catch (IOException e) {
            LOGGER.error("Failed to read manifest file", e);
            return versionUrls = Collections.emptyMap();
        }

        versionUrls = new HashMap<>();
        for (VersionManifest.Version version : manifest.versions) {
            if ("release".equals(version.type)) {
                versionUrls.put(version.id, version.url);
            }
        }
        return versionUrls;
    }

    private static String getAssetUrl(String version) {
        if (versionAssetUrls.containsKey(version))
            return versionAssetUrls.get(version);
        URL versionUrl;
        try {
            String url = getVersionUrls().get(version);
            if (url == null) {
                LOGGER.error("No version URL found for version " + version);
                versionAssetUrls.put(version, null);
                return null;
            }
            versionUrl = new URL(url);
        } catch (MalformedURLException e) {
            LOGGER.error("Malformed version URL for version " + version);
            versionAssetUrls.put(version, null);
            return null;
        }

        File versionFile = download(versionUrl, version + "/" + version + ".json");
        if (versionFile == null) {
            versionAssetUrls.put(version, null);
            return null;
        }

        VersionFile file;
        try {
            file = GSON.fromJson(new FileReader(versionFile), VersionFile.class);
        } catch (IOException e) {
            LOGGER.error("Failed to read version file for version " + version, e);
            versionAssetUrls.put(version, null);
            return null;
        }
        versionAssetUrls.put(version, file.assetIndex.url);
        return file.assetIndex.url;
    }

    private static Map<String, String> getLangFileUrls(String version) {
        if (langFileUrls.containsKey(version))
            return langFileUrls.get(version);

        URL assetUrl;
        try {
            String url = getAssetUrl(version);
            if (url == null) {
                LOGGER.error("No asset URL found for version " + version);
                langFileUrls.put(version, Collections.emptyMap());
                return Collections.emptyMap();
            }
            assetUrl = new URL(url);
        } catch (MalformedURLException e) {
            LOGGER.error("Malformed asset URL for version " + version);
            langFileUrls.put(version, Collections.emptyMap());
            return Collections.emptyMap();
        }

        File assetFile = download(assetUrl, version + "/indexes.json");
        if (assetFile == null) {
            langFileUrls.put(version, Collections.emptyMap());
            return Collections.emptyMap();
        }

        AssetFile assets;
        try {
            assets = GSON.fromJson(new FileReader(assetFile), AssetFile.class);
        } catch (IOException e) {
            LOGGER.error("Failed to read asset file for version " + version, e);
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

    private static File getLangFile(String version, String langCode) {
        langCode = langCode.toLowerCase(Locale.ENGLISH);
        // en_us is in the jar file rather than in the assets, we don't want to have to download the whole jar
        if ("en_us".equals(langCode))
            langCode = "en_gb";

        if (langFiles.computeIfAbsent(version, k -> new HashMap<>()).containsKey(langCode))
            return langFiles.get(version).get(langCode);

        URL langUrl;
        try {
            String url = getLangFileUrls(version).get(langCode);
            if (url == null) {
                langFiles.get(version).put(langCode, null);
                return null;
            }
            langUrl = new URL(url);
        } catch (MalformedURLException e) {
            LOGGER.error("Malformed lang url for " + version + "/" + langCode);
            langFiles.get(version).put(langCode, null);
            return null;
        }

        File langFile = download(langUrl, version + "/" + langCode + ".lang");
        langFiles.get(version).put(langCode, langFile);
        return langFile;
    }

    private static Map<String, String> getTranslations(String version, String langCode) {
        File langFile = getLangFile(version, langCode);
        if (langFile == null)
            return Collections.emptyMap();

        try {
            //noinspection unchecked
            Map<String, Object> translations = GSON.fromJson(new FileReader(langFile), Map.class);
            translations.keySet().removeIf(k -> !(translations.get(k) instanceof String));
            //noinspection unchecked
            return (Map<String, String>) (Object) translations;
        } catch (JsonSyntaxException e) {
            // expected - old lang file format
        } catch (IOException e) {
            LOGGER.error("Failed to read lang file " + version + "/" + langCode, e);
            return Collections.emptyMap();
        }

        Map<String, String> translations = new HashMap<>();
        try {
            for (String line : FileUtils.readLines(langFile, StandardCharsets.UTF_8)) {
                if (line.contains("=") && !line.startsWith("#")) {
                    String[] parts = line.split("=", 2);
                    translations.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read lang file in old format " + version + "/" + langCode, e);
            return Collections.emptyMap();
        }
        return translations;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static File download(URL url, String dest) {
        File destFile = new File(CACHE_DIR, dest);
        File etagFile = new File(CACHE_DIR, dest + ".etag");

        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (destFile.exists() && etagFile.exists()) {
                String etag = FileUtils.readFileToString(etagFile, StandardCharsets.UTF_8);
                connection.setRequestProperty("If-None-Match", etag);
            }

            connection.setRequestProperty("Accept-Encoding", "gzip");

            connection.connect();

            int responseCode = connection.getResponseCode();
            if ((responseCode < 200 || responseCode > 299) && responseCode != HttpURLConnection.HTTP_NOT_MODIFIED) {
                throw new IOException("Got HTTP " + responseCode + " from " + url);
            }

            long lastModified = connection.getHeaderFieldDate("Last-Modified", -1);
            if (destFile.exists() && (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED || lastModified > 0 && destFile.lastModified() >= lastModified))
                return destFile;

            destFile.getParentFile().mkdirs();
            try {
                FileUtils.copyInputStreamToFile(connection.getInputStream(), destFile);
            } catch (IOException e) {
                destFile.delete();
                throw e;
            }

            if (lastModified > 0)
                destFile.setLastModified(lastModified);

            String etag = connection.getHeaderField("ETag");
            if (etag != null) {
                FileUtils.writeStringToFile(etagFile, etag, StandardCharsets.UTF_8);
            }
            return destFile;
        } catch (UnknownHostException e) {
            return destFile.exists() ? destFile : null;
        } catch (IOException e) {
            LOGGER.error("Error downloading file " + dest + " from " + url);
            return null;
        }
    }

    static class VersionManifest {
        List<Version> versions;
        static class Version {
            String id;
            String type;
            String url;
        }
    }

    static class VersionFile {
        AssetIndex assetIndex;
        static class AssetIndex {
            String url;
        }
    }

    static class AssetFile {
        Map<String, Obj> objects;
        static class Obj {
            String hash;
        }
    }

}
