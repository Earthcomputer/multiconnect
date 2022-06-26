package net.earthcomputer.multiconnect.debug;

import com.google.common.html.HtmlEscapers;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class PacketVisualizer {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Language("CSS")
    public static final String STYLESHEET = """
            body {
                color: rebeccapurple;
                font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
            }
            .entry {
                border: 1px solid black;
                border-radius: 5px;
                padding: 3px;
                display: inline-block;
            }
            .null {
                background-color: pink;
            }
            .primitive {
                background-color: lightblue;
            }
            .primitive_list {
                background-color: lightblue;
            }
            .primitive_list_table {
                display: inline-grid;
                grid-template-columns: repeat(11, 1fr);
                grid-column-gap: 10px;
            }
            .object_list {
                background-color: burlywood;
            }
            .record_table {
                display: inline-grid;
                grid-template-columns: repeat(2, max-content);
                grid-column-gap: 10px;
            }
            .table_key {
                color: black;
                font-weight: bold;
            }
            .nbt {
                background-color: lightgreen;
            }
            .message_variant {
                background-color: pink;
            }
            """;

    @Language("JS")
    public static final String SCRIPT_EPILOGUE = """
            async function loadPage(number, details) {
                const req = await fetch(`packets/${number}.html`);
                if (req.status !== 200) {
                    alert(`failed to load ${req.status}: \\n${req.statusText}`);
                    return;
                }
                const text = await req.text();
                const parser = new DOMParser();
                const doc = parser.parseFromString(text, "text/html").getElementsByTagName("body")[0];
                details.innerHTML = doc.innerHTML;
            }
            async function onDetailsClick(details) {
                if (details.hasAttribute("open")) {
                    const number = details.getAttribute("data-number");
                    loadPage(number, details.children[1]);
                } else {
                    details.children[1].innerHTML = "";
                }
            }
            """;

    private PacketVisualizer() {
    }

    public static void reset() {
        Path packetsDir = FabricLoader.getInstance().getConfigDir().resolve("multiconnect").resolve("packet-logs").resolve("packets");
        try (Stream<Path> files = Files.list(packetsDir)) {
            files.forEach(file -> {
                try {
                    Files.delete(file);
                } catch (IOException e) {
                    LOGGER.error("Failed to delete file " + file, e);
                }
            });
        } catch (IOException ignore) {
        }
        try {
            Files.createDirectories(packetsDir);
        } catch (IOException e) {
            LOGGER.error("Failed to create packets directory", e);
        }
    }

    @Language("HTML")
    public static String visualize(Object packet, boolean clientbound, int packetCounter) {
        @Language("HTML")
        String url = "packets/" + packetCounter + ".html";
        try (BufferedWriter writer = Files.newBufferedWriter(FabricLoader.getInstance().getConfigDir().resolve("multiconnect").resolve("packet-logs").resolve(url))) {
            writer.write("<!DOCTYPE html>\n");
            writer.write("<html>\n");
            writer.write("<head>\n");
            writer.write("<meta charset=\"UTF-8\">\n");
            writer.write("<style>\n");
            writer.write(STYLESHEET + "\n");
            writer.write("</style>\n");
            writer.write("</head>\n");
            writer.write("<body>\n");
            writer.write(messageVariantContents(packet, clientbound));
            writer.write("</body>\n");
            writer.write("</html>\n");
        } catch (IOException e) {
            LOGGER.error("Error writing packet log", e);
        }
        return "<span class='entry message_variant'><details ontoggle='onDetailsClick(this)' data-number='" + packetCounter + "'><summary>Click to expand " + getPresentableClassName(packet.getClass()) + "</summary><div></div></details></span>";
    }

    @SuppressWarnings("unchecked")
    @Language("HTML")
    private static <T> String objectToHtml(Object object, @Nullable Registries registry, boolean clientbound, boolean wantOutline) {
        // handle null first to avoid NPEs
        if (object == null) {
            return stringEntry("null", EntryType.NULL, wantOutline);
        }

        // handle simple stringifiable types
        if (object instanceof Number || object instanceof Boolean || object instanceof UUID || object instanceof Identifier) {
            // check if there is an associated registry to display something more useful
            if (registry != null) {
                if (object instanceof Byte || object instanceof Short || object instanceof Integer || object instanceof Long) {
                    Integer rawId = ((Number) object).intValue();
                    DebugUtils.RegistryLike<T> registryLike = (DebugUtils.RegistryLike<T>) DebugUtils.getRegistryLike(registry);
                    if (clientbound) {
                        rawId = registryLike.serverRawIdToClient(rawId);
                    }
                    String text;
                    if (rawId == null) {
                        text = object.toString();
                    } else {
                        text = object + " // " + registryLike.getName(registryLike.getValue(rawId));
                    }
                    return stringEntry(text, EntryType.PRIMITIVE, wantOutline);
                }
            }

            return stringEntry(object.toString(), EntryType.PRIMITIVE, wantOutline);
        }
        if (object instanceof Enum<?> en) {
            return stringEntry(en.name(), EntryType.PRIMITIVE, wantOutline);
        }
        if (object instanceof String str) {
            return entry("&quot;"
                            + HtmlEscapers.htmlEscaper().escape(str)
                            .replace("&quot;", "<span style='color: orange;'>&quot;</span>")
                            + "&quot;",
                    EntryType.PRIMITIVE, wantOutline);
        }

        // optionals
        if (object instanceof OptionalInt optInt) {
            if (optInt.isEmpty()) {
                return stringEntry("OptionalInt.empty()", EntryType.PRIMITIVE, wantOutline);
            } else {
                return objectToHtml(optInt.getAsInt(), registry, clientbound, wantOutline);
            }
        }
        if (object instanceof OptionalLong optLong) {
            if (optLong.isEmpty()) {
                return stringEntry("OptionalLong.empty()", EntryType.PRIMITIVE, wantOutline);
            } else {
                return objectToHtml(optLong.getAsLong(), registry, clientbound, wantOutline);
            }
        }
        if (object instanceof Optional<?> opt) {
            if (opt.isEmpty()) {
                return stringEntry("Optional.empty()", EntryType.PRIMITIVE, wantOutline);
            } else {
                return objectToHtml(opt.get(), registry, clientbound, wantOutline);
            }
        }

        // arrays
        if (object instanceof byte[] bytes) {
            return makePrimitiveList(IntStream.range(0, bytes.length).mapToObj(i -> bytes[i]), registry, clientbound, wantOutline);
        }
        if (object instanceof short[] shorts) {
            return makePrimitiveList(IntStream.range(0, shorts.length).mapToObj(i -> shorts[i]), registry, clientbound, wantOutline);
        }
        if (object instanceof int[] ints) {
            return makePrimitiveList(Arrays.stream(ints).boxed(), registry, clientbound, wantOutline);
        }
        if (object instanceof long[] longs) {
            return makePrimitiveList(Arrays.stream(longs).boxed(), registry, clientbound, wantOutline);
        }
        if (object instanceof float[] floats) {
            return makePrimitiveList(IntStream.range(0, floats.length).mapToObj(i -> floats[i]), registry, clientbound, wantOutline);
        }
        if (object instanceof double[] doubles) {
            return makePrimitiveList(Arrays.stream(doubles).boxed(), registry, clientbound, wantOutline);
        }
        if (object.getClass().isArray()) {
            Object[] arr = (Object[]) object;
            return makeObjectList(Arrays.stream(arr), registry, clientbound, wantOutline);
        }

        // lists
        if (object instanceof IntList ints) {
            return makePrimitiveList(ints.intStream().boxed(), registry, clientbound, wantOutline);
        }
        if (object instanceof LongList longs) {
            return makePrimitiveList(longs.longStream().boxed(), registry, clientbound, wantOutline);
        }
        if (object instanceof List<?> list) {
            return makeObjectList(list.stream(), registry, clientbound, wantOutline);
        }

        // bit set
        if (object instanceof BitSet bitSet) {
            return makePrimitiveList(IntStream.range(0, bitSet.length()).mapToObj(i -> bitSet.get(i) ? "1" : "0"), null, clientbound, wantOutline);
        }

        // nbt
        if (object instanceof NbtCompound compound) {
            return nbtToHtml(compound, wantOutline);
        }

        // message variants
        return messageVariantToHtml(object, clientbound, wantOutline);
    }

    @Language("HTML")
    private static String nbtToHtml(NbtElement nbt, boolean wantOutline) {
        if (nbt instanceof NbtCompound compound) {
            StringBuilder text = new StringBuilder(
                    "<details><summary>Click to expand NBT Compound</summary><div class='record_table'>"
            );
            for (String key : compound.getKeys()) {
                text.append("<span class='table_key'>").append(key).append(": </span>");
                text.append("<span>").append(nbtToHtml(compound.get(key), true)).append("</span>");
            }
            text.append("</div></details>");
            return entry(text.toString(), EntryType.NBT, wantOutline);
        }
        if (nbt instanceof AbstractNbtList<?> list) {
            if (nbt instanceof NbtList) {
                return makeObjectList(list.stream(), null, false, wantOutline);
            } else if (nbt instanceof NbtByteArray byteArray) {
                return objectToHtml(byteArray.getByteArray(), null, false, wantOutline);
            } else if (nbt instanceof NbtIntArray intArray) {
                return objectToHtml(intArray.getIntArray(), null, false, wantOutline);
            } else if (nbt instanceof NbtLongArray longArray) {
                return objectToHtml(longArray.getLongArray(), null, false, wantOutline);
            } else {
                throw new IllegalStateException("Unknown NbtList type: " + nbt.getClass());
            }
        }
        if (nbt instanceof AbstractNbtNumber number) {
            return stringEntry(number.numberValue().toString(), EntryType.PRIMITIVE, wantOutline);
        }
        if (nbt instanceof NbtString str) {
            return objectToHtml(str.asString(), null, false, wantOutline);
        }
        throw new IllegalStateException("Unknown NbtElement type: " + nbt.getClass());
    }

    @Language("HTML")
    private static String messageVariantToHtml(Object object, boolean clientbound, boolean wantOutline) {
        Class<?> clazz = object.getClass();

        StringBuilder ret = new StringBuilder("<details><summary>Click to expand ")
                .append(getPresentableClassName(clazz))
                .append("</summary>");
        ret.append(messageVariantContents(object, clientbound));
        ret.append("</details>");
        return entry(ret.toString(), EntryType.MESSAGE_VARIANT, wantOutline);
    }

    @Language("HTML")
    private static String messageVariantContents(Object object, boolean clientbound) {
        StringBuilder ret = new StringBuilder("<div class='record_table'>");
        Class<?> clazz = object.getClass();
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && superclass != Object.class) {
            appendFields(object, superclass, ret, clientbound);
        }
        appendFields(object, clazz, ret, clientbound);
        ret.append("</div>");
        return ret.toString();
    }

    @NotNull
    private static String getPresentableClassName(Class<?> clazz) {
        String className = clazz.getName();
        return className.substring(className.lastIndexOf('.') + 1).replace('$', '.');
    }

    private static void appendFields(Object object, Class<?> clazz, StringBuilder sb, boolean clientbound) {
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            try {
                Registries registry = null;
                Registry registryAnnotation = field.getAnnotation(Registry.class);
                if (registryAnnotation != null) {
                    registry = registryAnnotation.value();
                }

                Object value = field.get(object);
                sb.append("<span class='table_key'>").append(field.getName()).append(": </span>");
                sb.append("<span>").append(objectToHtml(value, registry, clientbound, true)).append("</span>");
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Language("HTML")
    private static <T> String makePrimitiveList(Stream<T> list, @Nullable Registries registry, boolean clientbound, boolean wantOutline) {
        if (registry != null) {
            // we don't want to pack up registry objects, they're too wide
            return makeObjectList(list, registry, clientbound, wantOutline);
        }

        StringBuilder text = new StringBuilder(
                "<details><summary>Click to expand list</summary><div class='primitive_list_table'>"
        );
        int[] count = {0};
        list.forEach(obj -> {
            if (count[0] % 10 == 0) {
                text.append("<span class='table_key'>").append(count[0]).append(": </span>");
            }
            count[0]++;
            text.append("<span>").append(objectToHtml(obj, registry, clientbound, false)).append("</span>");
        });
        text.append("</div></details>");
        return entry(text.toString(), EntryType.PRIMITIVE_LIST, wantOutline);
    }

    private static <T> String makeObjectList(Stream<T> list, @Nullable Registries registry, boolean clientbound, boolean wantOutline) {
        StringBuilder text = new StringBuilder(
                "<details><summary>Click to expand list</summary><ol start=\"0\">"
        );
        list.forEach(obj -> {
            text.append("<li>").append(objectToHtml(obj, registry, clientbound, true)).append("</li>");
        });
        text.append("</ol></details>");
        return entry(text.toString(), EntryType.OBJECT_LIST, wantOutline);
    }

    private static String stringEntry(String text, EntryType type, boolean wantOutline) {
        return entry(HtmlEscapers.htmlEscaper().escape(text), type, wantOutline);
    }

    @Language("HTML")
    private static String entry(@Language("HTML") String text, EntryType type, boolean wantOutline) {
        if (wantOutline) {
            return "<span class='entry " + type.name().toLowerCase(Locale.ROOT) + "'>" + text + "</span>";
        } else {
            return text;
        }
    }

    private enum EntryType {
        NULL,
        PRIMITIVE,
        PRIMITIVE_LIST,
        OBJECT_LIST,
        NBT,
        MESSAGE_VARIANT,
    }
}
