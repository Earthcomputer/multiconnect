package net.earthcomputer.multiconnect.tools.command;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import joptsimple.OptionSet;
import net.earthcomputer.multiconnect.tools.Util;
import net.earthcomputer.multiconnect.tools.csv.CsvUtil;
import net.earthcomputer.multiconnect.tools.csv.CsvVisitor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DiffCommand extends CommandBase {
    @Override
    public String getName() {
        return "diffCsv";
    }

    @Override
    public boolean run(List<String> args, OptionSet options) throws IOException {
        if (args.size() < 2) {
            return false;
        }

        int[] idIndex = new int[1];
        List<String> aLines = new ArrayList<>();
        List<String> bLines = new ArrayList<>();

        CsvUtil.readCsv(Path.of(args.get(0)), new CsvVisitor() {
            @Override
            public void visitHeader(List<String> keys) {
                idIndex[0] = keys.indexOf("id");
                aLines.add(String.join(" ", keys));
            }

            @Override
            public void visitRow(List<String> row) {
                aLines.add(String.join(" ", row));
            }
        });

        CsvUtil.readCsv(Path.of(args.get(1)), new CsvVisitor() {
            @Override
            public void visitHeader(List<String> keys) {
                String row = String.join(" ", keys);
                if (!row.equals(aLines.get(0))) {
                    throw new RuntimeException("CSV headers don't match");
                }
                bLines.add(row);
            }

            @Override
            public void visitRow(List<String> row) {
                bLines.add(String.join(" ", row));
            }
        });

        Patch<String> patch = DiffUtils.diff(aLines, bLines, (a, b) -> {
            String[] aParts = a.split(" ");
            String[] bParts = b.split(" ");
            List<String> aList = new ArrayList<>(aParts.length);
            Collections.addAll(aList, aParts);
            if (idIndex[0] < aList.size()) {
                aList.remove(idIndex[0]);
            }
            List<String> bList = new ArrayList<>(bParts.length);
            Collections.addAll(bList, bParts);
            if (idIndex[0] < bList.size()) {
                bList.remove(idIndex[0]);
            }
            return aList.equals(bList);
        });
        List<String> diff = UnifiedDiffUtils.generateUnifiedDiff(args.get(0), args.get(1), aLines, patch, 3);
        if (diff.isEmpty()) {
            System.out.println("CSV files are identical.");
            return true;
        }
        for (String line : diff) {
            if (line.startsWith("+")) {
                System.out.println(Util.ANSI_GREEN + line + Util.ANSI_RESET);
            } else if (line.startsWith("-")) {
                System.out.println(Util.ANSI_RED + line + Util.ANSI_RESET);
            } else if (line.startsWith("@")) {
                System.out.println(Util.ANSI_YELLOW + line + Util.ANSI_RESET);
            } else {
                System.out.println(line);
            }
        }

        return true;
    }

    @Override
    public void printHelp(List<String> args, OptionSet options) {
        System.out.println("<a> <b>");
    }
}
