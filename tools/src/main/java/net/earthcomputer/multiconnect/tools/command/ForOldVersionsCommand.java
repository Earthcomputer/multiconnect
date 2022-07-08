package net.earthcomputer.multiconnect.tools.command;

import joptsimple.OptionSet;
import net.earthcomputer.multiconnect.tools.Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class ForOldVersionsCommand extends CommandBase {
    @Override
    public String getName() {
        return "forOldVersions";
    }

    @Override
    public boolean run(List<String> args, OptionSet options) throws IOException {
        if (args.isEmpty()) {
            return false;
        }

        List<String> versions = new ArrayList<>();
        try (Stream<Path> files = Files.list(Path.of("data"))) {
            files.forEach(file -> {
                if (Files.isDirectory(file)) {
                    versions.add(file.getFileName().toString());
                }
            });
        }
        if (versions.isEmpty()) {
            return true;
        }
        versions.sort(Util::compareVersions);
        versions.remove(versions.size() - 1);

        for (String version : versions) {
            try {
                int result = new ProcessBuilder()
                    .command(args)
                    .inheritIO()
                    .directory(new File("data", version))
                    .start()
                    .waitFor();
                if (result != 0) {
                    System.err.println("Process exited with code " + result + " for version " + version);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    @Override
    public void printHelp(List<String> args, OptionSet options) {

    }
}
