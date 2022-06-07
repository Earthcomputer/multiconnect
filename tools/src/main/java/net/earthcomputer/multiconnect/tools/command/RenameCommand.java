package net.earthcomputer.multiconnect.tools.command;

import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.earthcomputer.multiconnect.tools.Main;
import net.earthcomputer.multiconnect.tools.Util;
import net.earthcomputer.multiconnect.tools.csv.CsvUtil;
import net.earthcomputer.multiconnect.tools.csv.CsvVisitor;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class RenameCommand extends CommandBase {
    private static final List<OptionSpec<?>> OPTIONAL_FLAGS = Arrays.asList(Main.KEEP_OLD_NAME, Main.KEEP_OLD_NAME_UNTIL, Main.KEEP_OLD_NAME_SINCE);

    @Override
    public String getName() {
        return "rename";
    }

    @Override
    public boolean run(List<String> args, OptionSet options) throws IOException {
        if (args.size() < 3) {
            return false;
        }

        String registry = args.get(0);
        String oldName = Util.normalizeIdentifier(args.get(1));
        String newName = args.get(2);

        boolean keepOldName = options.has(Main.KEEP_OLD_NAME);
        @Nullable String keepUntil = options.valueOf(Main.KEEP_OLD_NAME_UNTIL);
        @Nullable String keepSince = options.valueOf(Main.KEEP_OLD_NAME_SINCE);

        Path dataDir = Path.of("data");
        if (!Files.exists(dataDir)) {
            System.out.println("Could not find data dir at " + dataDir.toAbsolutePath());
            return true;
        }

        try (var children = Files.list(dataDir)) {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            children.filter(it -> Files.isDirectory(it) && Main.VERSION_REGEX.matcher(it.getFileName().toString()).matches())
                    .forEach(child -> {
                String version = child.getFileName().toString();
                Path registryFile = child.resolve(registry + ".csv");
                if (Files.exists(registryFile) && Files.isReadable(registryFile)) {
                    futures.add(CompletableFuture.runAsync(() -> fixRegistryFile(registry, version, registryFile, oldName, newName, keepOldName, keepUntil, keepSince)));
                }
            });

            for (CompletableFuture<Void> future : futures) {
                future.join();
            }
        }

        System.out.println("Done.");

        return true;
    }

    private static void fixRegistryFile(
            String registry,
            String version,
            Path registryFile,
            String oldName,
            String newName,
            boolean keepOldName,
            @Nullable String keepUntil,
            @Nullable String keepSince
    ) {

        boolean shouldKeepOldName = keepOldName
                || (keepUntil != null && Util.compareVersions(version, keepUntil) <= 0)
                || (keepSince != null && Util.compareVersions(version, keepSince) >= 0);

        try {
            CsvUtil.modifyCsv(registryFile, new CsvVisitor() {
                int nameIndex;
                int oldNameIndex;

                @Override
                public void visitHeader(List<String> keys) {
                    nameIndex = keys.indexOf("name");
                    oldNameIndex = keys.indexOf("oldName");
                }

                @Override
                public void visitRow(List<String> row) {
                    String currentName = Util.getOrNull(row, nameIndex);
                    if (!oldName.equals(Util.normalizeIdentifier(currentName))) {
                        return;
                    }

                    if (shouldKeepOldName && Util.getOrNull(row, oldNameIndex) == null) {
                        CsvUtil.set(row, oldNameIndex, currentName);
                    }

                    CsvUtil.set(row, nameIndex, newName);
                }
            });

            if (registry.equals("block")) {
                CsvUtil.modifyCsv(registryFile.resolveSibling("block_state.csv"), new CsvVisitor() {
                    int nameIndex;
                    int oldNameIndex;

                    @Override
                    public void visitHeader(List<String> keys) {
                        nameIndex = keys.indexOf("name");
                        oldNameIndex = keys.indexOf("oldName");
                    }

                    @Override
                    public void visitRow(List<String> row) {
                        String currentName = Util.getOrNull(row, nameIndex);
                        if (currentName == null) {
                            return;
                        }
                        String oldBlockName = Util.substringBefore(currentName, '[');
                        if (!Util.normalizeIdentifier(oldBlockName).equals(oldName)) {
                            return;
                        }
                        if (shouldKeepOldName && Util.getOrNull(row, oldNameIndex) == null) {
                            CsvUtil.set(row, oldNameIndex, currentName);
                        }
                        CsvUtil.set(row, nameIndex, newName + currentName.substring(oldBlockName.length()));
                    }
                });
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void printHelp(List<String> args, OptionSet options) {
        System.out.println("<registry> <old-name> <new-name>");
    }

    @Override
    public List<OptionSpec<?>> getOptionalFlags() {
        return OPTIONAL_FLAGS;
    }
}
