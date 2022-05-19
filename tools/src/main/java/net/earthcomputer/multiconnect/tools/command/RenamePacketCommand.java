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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

public final class RenamePacketCommand extends CommandBase {
    private static final List<OptionSpec<?>> OPTIONAL_FLAGS = Arrays.asList(Main.MIN_VERSION, Main.MAX_VERSION);

    @Override
    public String getName() {
        return "renamePacket";
    }

    @Override
    public boolean run(List<String> args, OptionSet options) throws IOException {
        if (args.size() < 2) {
            return false;
        }

        Pattern fromRegex;
        try {
            fromRegex = Pattern.compile(args.get(0));
        } catch (PatternSyntaxException e) {
            String message = e.getMessage() != null ? e.getMessage() : args.get(0);
            System.out.println("Invalid regex: " + message);
            return true;
        }

        String replacement = args.get(1);

        @Nullable String minVersion = options.valueOf(Main.MIN_VERSION);
        @Nullable String maxVersion = options.valueOf(Main.MAX_VERSION);

        Path dataDir = Path.of("data");
        if (!Files.exists(dataDir)) {
            System.out.println("Could not find data dir at " + dataDir.toAbsolutePath());
            return true;
        }

        try (var children = Files.list(dataDir)) {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            children.filter(child -> {
                if (!Files.isDirectory(child)) {
                    return false;
                }
                String version = child.getFileName().toString();
                if (!Main.VERSION_REGEX.matcher(version).matches()) {
                    return false;
                }
                if (minVersion != null && Util.compareVersions(version, minVersion) < 0) {
                    return false;
                }
                if (maxVersion != null && Util.compareVersions(version, maxVersion) > 0) {
                    return false;
                }
                return true;
            })
                    .flatMap(child -> Stream.of(child.resolve("cpackets.csv"), child.resolve("spackets.csv")))
                    .forEach(csvFile -> {
                        if (Files.exists(csvFile) && Files.isReadable(csvFile)) {
                            futures.add(CompletableFuture.runAsync(() -> doRename(csvFile, fromRegex, replacement)));
                        }
                    });

            for (CompletableFuture<Void> future : futures) {
                future.join();
            }
        }

        System.out.println("Done.");

        return true;
    }

    private static void doRename(Path csvFile, Pattern fromRegex, String replacement) {
        try {
            CsvUtil.modifyCsv(csvFile, new CsvVisitor() {
                int clazzIndex;

                @Override
                public void visitHeader(List<String> keys) {
                    clazzIndex = keys.indexOf("clazz");
                }

                @Override
                public void visitRow(List<String> row) {
                    String clazz = Util.getOrNull(row, clazzIndex);
                    if (clazz == null) {
                        return;
                    }
                    String newClazz = fromRegex.matcher(clazz).replaceAll(replacement);
                    CsvUtil.set(row, clazzIndex, newClazz);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void printHelp(List<String> args, OptionSet options) {
        System.out.println("<from-regex> <replacement>");
    }

    @Override
    public List<OptionSpec<?>> getOptionalFlags() {
        return OPTIONAL_FLAGS;
    }
}
