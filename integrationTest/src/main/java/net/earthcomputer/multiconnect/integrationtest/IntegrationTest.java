package net.earthcomputer.multiconnect.integrationtest;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class IntegrationTest implements ModInitializer {
    @Override
    public void onInitialize() {
        try {
            syncMacrosFolder();
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static void syncMacrosFolder() throws IOException, URISyntaxException {
        Path jsMacrosDir = FabricLoader.getInstance().getConfigDir().resolve("jsMacros").resolve("Macros");
        if (Files.isSymbolicLink(jsMacrosDir)) {
            return;
        }

        recursiveDelete(jsMacrosDir);
        if (!Files.exists(jsMacrosDir)) {
            Files.createDirectories(jsMacrosDir);
        }

        File modFile = new File(IntegrationTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        if (modFile.isDirectory()) {
            modFile = new File(modFile.getAbsolutePath().replace("classes" + File.separator + "java", "resources"));
            Path fromMacrosDir = modFile.toPath().resolve("Macros");
            try (Stream<Path> paths = Files.walk(fromMacrosDir)) {
                for (Path path : (Iterable<Path>) paths::iterator) {
                    if (!Files.isDirectory(path)) {
                        try (InputStream from = Files.newInputStream(path)) {
                            copyFile(fromMacrosDir.relativize(path).toString().replace(File.separator, "/"), from, jsMacrosDir);
                        }
                    }
                }
            }
        } else {
            try (JarFile modJar = new JarFile(modFile)) {
                Enumeration<JarEntry> entries = modJar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().startsWith("Macros/") && !entry.isDirectory()) {
                        copyFile(entry.getName().substring("Macros/".length()), modJar.getInputStream(entry), jsMacrosDir);
                    }
                }
            }
        }
    }

    private static void recursiveDelete(Path dir) throws IOException {
        try (Stream<Path> files = Files.list(dir)) {
            for (Path file : (Iterable<Path>) files::iterator) {
                if (Files.isDirectory(file)) {
                    recursiveDelete(file);
                }
                Files.delete(file);
            }
        }
    }

    private static void copyFile(String name, InputStream from, Path jsMacrosDir) throws IOException {
        String[] parts = name.split("/");
        Path destPath = jsMacrosDir;
        for (int i = 0; i < parts.length - 1; i++) {
            destPath = destPath.resolve(parts[i]);
            if (!Files.exists(destPath)) {
                Files.createDirectory(destPath);
            }
        }
        destPath = destPath.resolve(parts[parts.length - 1]);
        Files.copy(from, destPath);
    }
}
