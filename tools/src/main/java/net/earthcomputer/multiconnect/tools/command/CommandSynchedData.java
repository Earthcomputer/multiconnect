package net.earthcomputer.multiconnect.tools.command;

import joptsimple.OptionSet;
import net.earthcomputer.multiconnect.tools.Util;
import net.earthcomputer.multiconnect.tools.csv.CsvUtil;
import net.earthcomputer.multiconnect.tools.csv.CsvVisitor;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CommandSynchedData extends CommandBase {
    @Override
    public String getName() {
        return "synchedData";
    }

    @Override
    public boolean run(List<String> args, OptionSet options) throws IOException {
        if (args.size() < 4) {
            return false;
        }
        Path dataDir = Path.of("data", args.get(0));
        if (!Files.isDirectory(dataDir)) {
            System.err.println("File not found: " + dataDir);
            return true;
        }

        String entityType = args.get(2);
        Integer id = Util.tryParse(args.get(3));
        if (id == null) {
            System.err.println("Invalid id " + args.get(3));
            return true;
        }

        return switch (args.get(1)) {
            case "preAdd" -> {
                add(dataDir, entityType, id);
                yield true;
            }
            case "postRemove" -> {
                remove(dataDir, entityType, id);
                yield true;
            }
            default -> false;
        };
    }

    private static void add(Path dataDir, String entityType, int id) throws IOException {
        var inheritanceData = loadInheritanceData(dataDir);
        if (inheritanceData == null) {
            return;
        }
        CsvUtil.modifyCsv(dataDir.resolve("synched_entity_data.csv"), new CsvVisitor() {
            int classIndex;
            int idIndex;

            @Override
            public void visitHeader(List<String> keys) {
                this.classIndex = keys.indexOf("class");
                this.idIndex = keys.indexOf("id");
            }

            @Override
            public void visitRow(List<String> row) {
                String subclass = Util.getOrNull(row, classIndex);
                Integer entryId = Util.tryParse(Util.getOrNull(row, idIndex));
                if (subclass != null && entryId != null && entryId >= id && isAssignable(inheritanceData, subclass, entityType)) {
                    CsvUtil.set(row, idIndex, String.valueOf(entryId + 1));
                }
            }
        });
    }

    private static void remove(Path dataDir, String entityType, int id) throws IOException {
        var inheritanceData = loadInheritanceData(dataDir);
        if (inheritanceData == null) {
            return;
        }
        CsvUtil.modifyCsv(dataDir.resolve("synched_entity_data.csv"), new CsvVisitor() {
            int classIndex;
            int idIndex;

            @Override
            public void visitHeader(List<String> keys) {
                this.classIndex = keys.indexOf("class");
                this.idIndex = keys.indexOf("id");
            }

            @Override
            public void visitRow(List<String> row) {
                String subclass = Util.getOrNull(row, classIndex);
                Integer entryId = Util.tryParse(Util.getOrNull(row, idIndex));
                if (subclass != null && entryId != null && entryId >= id && isAssignable(inheritanceData, subclass, entityType)) {
                    if (entryId == id) {
                        throw new RuntimeException("id has not been removed");
                    }
                    CsvUtil.set(row, idIndex, String.valueOf(entryId - 1));
                }
            }
        });
    }

    @Nullable
    private static Map<String, String> loadInheritanceData(Path dataDir) throws IOException {
        Map<String, String> inheritance = new HashMap<>();
        CsvUtil.readCsv(dataDir.resolve("entity_inheritance.csv"), new CsvVisitor() {
            int classIndex;
            int superclassIndex;

            @Override
            public void visitHeader(List<String> keys) {
                classIndex = keys.indexOf("class");
                superclassIndex = keys.indexOf("superclass");
            }

            @Override
            public void visitRow(List<String> row) {
                String clazz = Util.getOrNull(row, classIndex);
                String superclass = Util.getOrNull(row, superclassIndex);
                if (clazz != null && superclass != null) {
                    inheritance.put(clazz, superclass);
                }
            }
        });
        if (!detectCycles(inheritance)) {
            return null;
        }
        return inheritance;
    }

    private static boolean detectCycles(Map<String, String> inheritanceData) {
        for (String key : inheritanceData.keySet()) {
            String superclass = inheritanceData.get(key);
            while (superclass != null) {
                if (superclass.equals(key)) {
                    System.err.println("Cycle detected: " + key);
                    return false;
                }
                superclass = inheritanceData.get(superclass);
            }
        }
        return true;
    }

    private static boolean isAssignable(Map<String, String> inheritanceData, String subclass, String superclass) {
        do {
            if (subclass.equals(superclass)) {
                return true;
            }
            subclass = inheritanceData.get(subclass);
        } while (subclass != null);
        return false;
    }

    @Override
    public void printHelp(List<String> args, OptionSet options) {
        System.out.println("<version> <preAdd|postRemove> <entity-type> <id>");
    }
}
