package net.earthcomputer.multiconnect.protocols.generic;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import net.earthcomputer.multiconnect.impl.Multiconnect;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.zip.GZIPInputStream;

public final class FileDownloader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();

    private FileDownloader() {
    }
    private static final Path CACHE_DIR = FabricLoader.getInstance().getConfigDir().resolve("multiconnect").resolve("caches");

    public static URL createURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    // Downloads a json file if un-downloaded, or if it has a json syntax error (corrupted)
    @Nullable
    public static <T> T downloadJson(URL url, String dest, Class<T> type) {
        return downloadAndParse(url, dest, file -> {
            try (BufferedReader reader = Files.newBufferedReader(file)) {
                return GSON.fromJson(reader, type);
            }
        });
    }

    @Nullable
    public static Path download(URL url, String dest) {
        return download(url, dest, Downloader.DEFAULT, false);
    }

    @Nullable
    public static <T> T downloadAndParse(URL url, String dest, Parser<T> parser) {
        return downloadAndParse(url, dest, Downloader.DEFAULT, parser);
    }

    @Nullable
    public static <T> T downloadAndParse(URL url, String dest, Downloader downloader, Parser<T> parser) {
        Path downloadedFile = download(url, dest, downloader, false);
        if (downloadedFile == null) {
            return null;
        }
        try {
            T result = parser.parse(downloadedFile);
            if (result != null) {
                return result;
            }
        } catch (Exception ignored) {
        }

        // corrupted file, try redownloading
        try {
            Files.deleteIfExists(downloadedFile);
        } catch (IOException e1) {
            LOGGER.error("Failed to delete file {}", downloadedFile);
        }
        downloadedFile = download(url, dest, downloader, true);
        if (downloadedFile == null) {
            return null;
        }
        try {
            T result = parser.parse(downloadedFile);
            if (result != null) {
                return result;
            }
        } catch (Exception e1) {
            LOGGER.info("Failed to parse file {} downloaded from {}", dest, url, e1);
            return null;
        }
        LOGGER.info("Failed to parse file {} downloaded from {}", dest, url);
        return null;
    }

    @Nullable
    private static Path download(URL url, String dest, Downloader downloader, boolean force) {
        Path destFile = CACHE_DIR.resolve(dest);
        Path etagFile = CACHE_DIR.resolve(dest + ".etag");

        boolean destExists = Files.exists(destFile);

        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (!force && destExists && Files.exists(etagFile)) {
                String etag = Files.readString(etagFile);
                connection.setRequestProperty("If-None-Match", etag);
            }

            connection.setRequestProperty("Accept-Encoding", "gzip");
            connection.setRequestProperty("User-Agent", "multiconnect " + Multiconnect.getVersion());

            connection.connect();

            int responseCode = connection.getResponseCode();
            if ((responseCode < 200 || responseCode > 299) && responseCode != HttpURLConnection.HTTP_NOT_MODIFIED) {
                throw new IOException("Got HTTP " + responseCode + " from " + url);
            }

            long lastModified = connection.getHeaderFieldDate("Last-Modified", -1);
            if (!force && destExists && (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED || (lastModified > 0 && Files.getLastModifiedTime(destFile).toMillis() >= lastModified))) {
                return destFile;
            }

            if (destFile.getParent() != null) {
                Files.createDirectories(destFile.getParent());
            }
            try (InputStream stream = connection.getInputStream()) {
                InputStream is = stream;
                if ("gzip".equals(connection.getContentEncoding())) {
                    is = new GZIPInputStream(stream);
                }
                downloader.download(is, destFile);
            } catch (IOException e) {
                Files.deleteIfExists(destFile);
                throw e;
            }

            if (lastModified > 0) {
                Files.setLastModifiedTime(destFile, FileTime.fromMillis(lastModified));
            }

            String etag = connection.getHeaderField("ETag");
            if (etag != null) {
                Files.writeString(etagFile, etag);
            }
            return destFile;
        } catch (UnknownHostException e) {
            return Files.exists(destFile) ? destFile : null;
        } catch (IOException e) {
            LOGGER.error("Error downloading file {} from {}", dest, url, e);
            return null;
        }
    }

    @FunctionalInterface
    public interface Downloader {
        Downloader DEFAULT = (in, dest) -> Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);

        void download(InputStream in, Path dest) throws IOException;
    }

    @FunctionalInterface
    public interface Parser<T> {
        @Nullable
        T parse(Path file) throws Exception;
    }
}
