package net.earthcomputer.multiconnect.tools.command;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import joptsimple.OptionSet;
import net.earthcomputer.multiconnect.tools.Util;
import net.earthcomputer.multiconnect.tools.csv.CsvUtil;
import net.earthcomputer.multiconnect.tools.csv.CsvVisitor;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

public final class CommandFixStupid extends CommandBase {
    @Override
    public String getName() {
        return "fixStupid";
    }

    @Override
    public boolean run(List<String> args, OptionSet options) throws IOException {
        Path dataDir = Path.of("data");
        try(Stream<Path> dataChildren = Files.list(dataDir)) {
            dataChildren
                    .filter(it -> Files.isDirectory(it) && Util.compareVersions(it.getFileName().toString(), "1.12.2") <= 0)
                    .forEach(dir -> {
                        try {
                            fixVersionDir(dir);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }
        return true;
    }

    private static void fixVersionDir(Path dir) throws IOException {
        CsvUtil.modifyCsv(dir.resolve("item.csv"), new CsvVisitor() {
            int idIndex;
            int nameIndex;
            int nextId;

            @Override
            public void visitHeader(List<String> keys) {
                idIndex = keys.indexOf("id");
                nameIndex = keys.indexOf("name");
            }

            @Override
            public void visitRow(List<String> row) {
                Integer id = Util.tryParse(Util.getOrNull(row, idIndex));
                if (id == null) {
                    return;
                }

//                switch (id) {
//                    case 8 -> row.set(nameIndex, "multiconnect:flowing_water");
//                    case 10 -> row.set(nameIndex, "multiconnect:flowing_lava");
//                    case 43 -> row.set(nameIndex, "multiconnect:double_stone_slab");
//                    case 62 -> row.set(nameIndex, "multiconnect:lit_furnace");
//                    case 74 -> row.set(nameIndex, "multiconnect:lit_redstone_ore");
//                    case 75 -> row.set(nameIndex, "multiconnect:unlit_redstone_torch");
//                    case 94 -> row.set(nameIndex, "multiconnect:powered_repeater");
//                    case 124 -> row.set(nameIndex, "multiconnect:lit_redstone_lamp");
//                    case 125 -> row.set(nameIndex, "multiconnect:double_wooden_slab");
//                    case 150 -> row.set(nameIndex, "multiconnect:powered_comparator");
//                    case 178 -> row.set(nameIndex, "multiconnect:daylight_detector_inverted");
//                    case 181 -> row.set(nameIndex, "multiconnect:double_stone_slab2");
//                    case 204 -> row.set(nameIndex, "multiconnect:purpur_double_slab");
//                }

                if (id >= 65536) {
                    row.set(idIndex, String.valueOf(nextId++));
                } else {
                    nextId = id + 1;
                }
            }
        });
    }

    private static String jsonObjToString(JsonObject jobj) {
        String name = jobj.get("Name").getAsString();
        if (name.startsWith("minecraft:")) {
            name = name.substring(10);
        }
        if ("%%FILTER_ME%%".equals(name)) {
            name = "skull";
        }
        StringBuilder sb = new StringBuilder(name);
        if (!jobj.has("Properties")) {
            return sb.toString();
        }
        sb.append("[");
        var properties = jobj.get("Properties").getAsJsonObject().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList();
        boolean firstProp = true;
        for (Map.Entry<String, JsonElement> property : properties) {
            if (!firstProp) {
                sb.append(",");
            }
            firstProp = false;
            sb.append(property.getKey()).append("=").append(property.getValue().getAsString());
        }
        return sb.append("]").toString();
    }

    @Override
    public void printHelp(List<String> args, OptionSet options) {
    }
}
