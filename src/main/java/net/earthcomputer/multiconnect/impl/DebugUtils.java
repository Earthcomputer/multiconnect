package net.earthcomputer.multiconnect.impl;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.TypeRewriteRule;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.handler.timeout.TimeoutException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.api.ThreadSafe;
import net.earthcomputer.multiconnect.connect.ConnectionMode;
import net.earthcomputer.multiconnect.mixin.connect.ClientConnectionAccessor;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.generic.AbstractProtocol;
import net.earthcomputer.multiconnect.protocols.generic.Key;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketEncoderException;
import net.minecraft.state.property.Property;
import net.minecraft.text.BaseText;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class DebugUtils {
    private static final Logger LOGGER = LogManager.getLogger("multiconnect");
    private static final String MULTICONNECT_ISSUES_BASE_URL = "https://github.com/Earthcomputer/multiconnect/issues";
    private static final String MULTICONNECT_ISSUE_URL = MULTICONNECT_ISSUES_BASE_URL + "/%d";
    private static int rareBugIdThatOccurred = 0;
    private static long timeThatRareBugOccurred;
    public static String lastServerBrand = ClientBrandRetriever.VANILLA;
    public static final boolean UNIT_TEST_MODE = Boolean.getBoolean("multiconnect.unitTestMode");
    public static final boolean IGNORE_ERRORS = Boolean.getBoolean("multiconnect.ignoreErrors");
    public static final boolean DUMP_REGISTRIES = Boolean.getBoolean("multiconnect.dumpRegistries");
    public static final boolean SKIP_TRANSLATION = Boolean.getBoolean("multiconnect.skipTranslation");
    public static final boolean STORE_BUFS_FOR_HANDLER = Boolean.getBoolean("multiconnect.storeBufsForHandler");

    public static final Key<byte[]> STORED_BUF = Key.create("storedBuf");

    private static final Map<TrackedData<?>, String> TRACKED_DATA_NAMES = new IdentityHashMap<>();
    private static void computeTrackedDataNames() {
        Set<Class<?>> trackedDataHolders = new HashSet<>();
        for (Field field : EntityType.class.getFields()) {
            if (field.getType() == EntityType.class && Modifier.isStatic(field.getModifiers())) {
                if (field.getGenericType() instanceof ParameterizedType type) {
                    if (type.getActualTypeArguments()[0] instanceof Class<?> entityClass && Entity.class.isAssignableFrom(entityClass)) {
                        for (; entityClass != Object.class; entityClass = entityClass.getSuperclass()) {
                            trackedDataHolders.add(entityClass);
                        }
                    }
                }
            }
        }
        for (AbstractProtocol protocol : ProtocolRegistry.all()) {
            trackedDataHolders.add(protocol.getClass());
        }

        for (Class<?> trackedDataHolder : trackedDataHolders) {
            for (Field field : trackedDataHolder.getDeclaredFields()) {
                if (field.getType() == TrackedData.class && Modifier.isStatic(field.getModifiers())) {
                    field.setAccessible(true);
                    TrackedData<?> trackedData;
                    try {
                        trackedData = (TrackedData<?>) field.get(null);
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                    TRACKED_DATA_NAMES.put(trackedData, trackedDataHolder.getSimpleName() + "::" + field.getName());
                }
            }
        }
    }

    public static String getTrackedDataName(TrackedData<?> data) {
        if (TRACKED_DATA_NAMES.isEmpty()) {
            computeTrackedDataNames();
        }
        String name = TRACKED_DATA_NAMES.get(data);
        return name == null ? "unknown" : name;
    }

    public static String getAllTrackedData(Entity entity) {
        List<DataTracker.Entry<?>> allEntries = entity.getDataTracker().getAllEntries();
        if (allEntries == null || allEntries.isEmpty()) {
            return "<no entries>";
        }

        return allEntries.stream()
                .sorted(Comparator.comparingInt(entry -> entry.getData().getId()))
                .map(entry -> entry.getData().getId() + ": " + getTrackedDataName(entry.getData()) + " = " + entry.get())
                .collect(Collectors.joining("\n"));
    }

    public static void reportRareBug(int bugId) {
        rareBugIdThatOccurred = bugId;
        timeThatRareBugOccurred = System.nanoTime();

        MinecraftClient mc = MinecraftClient.getInstance();
        if (!mc.isOnThread()) {
            mc.send(() -> reportRareBug(bugId));
            return;
        }

        String url = MULTICONNECT_ISSUE_URL.formatted(rareBugIdThatOccurred);
        mc.inGameHud.getChatHud().addMessage(new TranslatableText("multiconnect.rareBug", new TranslatableText("multiconnect.rareBug.link")
                .styled(style -> style.withUnderline(true)
                        .withColor(Formatting.BLUE)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(url)))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))))
            .formatted(Formatting.YELLOW));
    }

    public static boolean wasRareBugReportedRecently() {
        return rareBugIdThatOccurred != 0 && (System.nanoTime() - timeThatRareBugOccurred) < 10_000_000_000L;
    }

    private static BaseText getRareBugText(int line) {
        String url = MULTICONNECT_ISSUE_URL.formatted(rareBugIdThatOccurred);
        return new TranslatableText("multiconnect.rareBug", new TranslatableText("multiconnect.rareBug.link")
                .styled(style -> style.withUnderline(true)
                        .withColor(Formatting.BLUE)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(url)))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))));
    }

    public static Screen createRareBugScreen(Screen parentScreen) {
        URL url;
        try {
            url = new URL(MULTICONNECT_ISSUE_URL.formatted(rareBugIdThatOccurred));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        rareBugIdThatOccurred = 0;
        return new ConfirmScreen(result -> {
            if (result) {
                Util.getOperatingSystem().open(url);
            }
            MinecraftClient.getInstance().setScreen(parentScreen);
        }, parentScreen.getTitle(), new TranslatableText("multiconnect.rareBug.screen"));
    }

    @ThreadSafe
    public static boolean isUnexpectedDisconnect(Throwable t) {
        return !(t instanceof PacketEncoderException) && !(t instanceof TimeoutException);
    }

    public static void wrapInErrorHandler(ByteBuf buf, String direction, Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            DebugUtils.logPacketError(buf, "Direction: " + direction);
            // consume all the input
            buf.readerIndex(buf.readerIndex() + buf.readableBytes());
            if (DebugUtils.IGNORE_ERRORS) {
                LOGGER.warn("Ignoring error in packet");
                e.printStackTrace();
            } else {
                throw e;
            }
        }
    }

    public static byte[] getBufData(ByteBuf buf) {
        if (buf.hasArray()) {
            return buf.array();
        }
        int prevReaderIndex = buf.readerIndex();
        buf.readerIndex(0);
        byte[] array = new byte[buf.readableBytes()];
        buf.readBytes(array);
        buf.readerIndex(prevReaderIndex);
        return array;
    }

    public static void logPacketError(ByteBuf data, String... extraLines) {
        logPacketError(getBufData(data), extraLines);
    }

    public static void logPacketError(byte[] data, String... extraLines) {
        LOGGER.error("!!!!!!!! Unexpected packet error, please upload this error to " + MULTICONNECT_ISSUES_BASE_URL + " !!!!!!!!");
        LOGGER.error("It may be helpful if you also provide the server IP, but you are not obliged to do this.");
        LOGGER.error("Minecraft version: {}", SharedConstants.getGameVersion().getName());
        FabricLoader.getInstance().getModContainer("multiconnect").ifPresent(modContainer -> {
            String version = modContainer.getMetadata().getVersion().getFriendlyString();
            LOGGER.error("multiconnect version: {}", version);
        });
        LOGGER.error("Server version: {} ({})", ConnectionInfo.protocolVersion, ConnectionMode.byValue(ConnectionInfo.protocolVersion).getName());
        LOGGER.error("Server brand: {}", lastServerBrand);
        for (String extraLine : extraLines) {
            LOGGER.error(extraLine);
        }
        LOGGER.error("Compressed packet data: {}", () -> {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            try (var out = new GZIPOutputStream(Base64.getEncoder().wrap(result))) {
                out.write(data);
            } catch (IOException e) {
                return "[error compressing] " + Base64.getEncoder().encodeToString(data);
            }
            return result.toString(StandardCharsets.UTF_8);
        });
    }

    public static void handlePacketDump(ClientPlayNetworkHandler networkHandler, String base64, boolean compressed) {
        byte[] bytes = decode(base64, compressed);
        if (bytes == null) {
            return;
        }
        LOGGER.info("Artificially handling packet of length {}", bytes.length);
        Channel channel = ((ClientConnectionAccessor) networkHandler.getConnection()).getChannel();
        assert channel != null;
        ChannelHandlerContext context = channel.pipeline().context("multiconnect_clientbound_translator");
        if (context == null) {
            context = channel.pipeline().context("decoder");
            if (context == null) {
                throw new IllegalStateException("Cannot handle packet dump on singleplayer");
            }
        }
        try {
            ByteBuf buf = Unpooled.wrappedBuffer(bytes);
            buf.readerIndex(0);
            if (channel.eventLoop().inEventLoop()) {
                ((ChannelInboundHandler) context.handler()).channelRead(context, buf);
            } else {
                ChannelHandlerContext context_f = context;
                channel.eventLoop().execute(() -> {
                    try {
                        ((ChannelInboundHandler) context_f.handler()).channelRead(context_f, buf);
                    } catch (Exception e) {
                        throw PacketIntrinsics.sneakyThrow(e);
                    }
                });
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static byte @Nullable [] decode(String base64, boolean compressed) {
        byte[] bytes = Base64.getDecoder().decode(base64);
        if (!compressed) {
            return bytes;
        }
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        try (var in = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            IOUtils.copy(in, result);
        } catch (IOException e) {
            LOGGER.error("Decompression error", e);
            return null;
        }
        return result.toByteArray();
    }

    @SuppressWarnings("unchecked")
    public static <T> void dumpRegistries() throws IOException {
        ConnectionMode connectionMode = ConnectionMode.byValue(ConnectionInfo.protocolVersion);
        File dir = new File("../data/" + connectionMode.getName());
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        dumpPackets(new File(dir, "spackets.csv"), NetworkSide.CLIENTBOUND, "SPacket", "S2CPacket");
        dumpPackets(new File(dir, "cpackets.csv"), NetworkSide.SERVERBOUND, "CPacket", "C2SPacket");
        for (Registries registries : Registries.values()) {
            Stream<Triple<Integer, String, String>> entries;
            if (registries == Registries.BLOCK_STATE) {
                entries = StreamSupport.stream(Block.STATE_IDS.spliterator(), false)
                        .filter(it -> {
                            Identifier name = Registry.BLOCK.getId(it.getBlock());
                            return !name.getNamespace().equals("multiconnect");
                        })
                        .map(it -> Triple.of(Block.STATE_IDS.getRawId(it), blockStateToString(it), blockStateToString(it)));
            } else if (registries == Registries.TRACKED_DATA_HANDLER) {
                List<Triple<Integer, String, String>> entryList = new ArrayList<>();
                TrackedDataHandler<?> handler;
                for (int i = 0; (handler = TrackedDataHandlerRegistry.get(i)) != null; i++) {
                    String name = null;
                    for (Field field : TrackedDataHandlerRegistry.class.getFields()) {
                        if (Modifier.isStatic(field.getModifiers()) && field.getType() == TrackedDataHandler.class) {
                            Object fieldValue;
                            try {
                                fieldValue = field.get(null);
                            } catch (ReflectiveOperationException e) {
                                throw new AssertionError(e);
                            }
                            if (handler == fieldValue) {
                                name = field.getName().toLowerCase(Locale.ROOT);
                                break;
                            }
                        }
                    }
                    if (name == null) {
                        throw new AssertionError("Could not find tracked data handler name for id " + i);
                    }
                    entryList.add(Triple.of(i, name, name));
                }
                entries = entryList.stream();
            } else {
                if (!registries.isRealRegistry()) {
                    throw new AssertionError("No way to dump registry " + registries);
                }
                Registry<T> registry;
                if (registries == Registries.MOTIVE) {
                    registry = (Registry<T>) Registry.PAINTING_MOTIVE;
                } else {
                    try {
                        registry = (Registry<T>) Registry.class.getDeclaredField(registries.name()).get(null);
                    } catch (ReflectiveOperationException e) {
                        throw new AssertionError(e);
                    }
                }
                entries = registry.stream()
                        .filter(it -> {
                            Identifier name = registry.getId(it);
                            return name == null || !name.getNamespace().equals("multiconnect");
                        })
                        .map(it -> {
                            Identifier name = registry.getId(it);
                            String nameStr = name == null ? "null" : name.getPath();
                            return Triple.of(registry.getRawId(it), nameStr, nameStr);
                        });
            }
            try (PrintWriter pw = new PrintWriter(new FileWriter(new File(dir, registries.name().toLowerCase(Locale.ROOT) + ".csv")))) {
                pw.println("id name oldName remapTo");
                entries.forEach(it -> {
                    if (it.getMiddle().equals(it.getRight())) {
                        pw.println(it.getLeft() + " " + it.getMiddle());
                    } else {
                        pw.println(it.getLeft() + " " + it.getMiddle() + " " + it.getRight());
                    }
                });
            }
        }
    }

    public static String blockStateToString(BlockState state) {
        Identifier unmodifiedName = Registry.BLOCK.getId(state.getBlock());
        String result = unmodifiedName.getPath();
        if (state.getBlock().getStateManager().getProperties().isEmpty()) return result;
        String stateStr = state.getBlock().getStateManager().getProperties().stream().map(it -> it.getName() + "=" + getName(it, state.get(it))).collect(Collectors.joining(","));
        return result + "[" + stateStr + "]";
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String getName(Property<T> prop, Object value) {
        return prop.name((T) value);
    }

    private static final Map<Class<? extends Packet<?>>, String> SUBST_NAME = ImmutableMap.<Class<? extends Packet<?>>, String>builder()
            .build();
    private static void dumpPackets(File output, NetworkSide side, String prefix, String toReplace) throws IOException {
        var packetHandlers = new ArrayList<>(NetworkState.PLAY.getPacketIdToPacketMap(side).int2ObjectEntrySet());
        packetHandlers.sort(Comparator.comparingInt(Int2ObjectMap.Entry::getIntKey));
        try (PrintWriter pw = new PrintWriter(new FileWriter(output))) {
            pw.println("id clazz");
            for (var entry : packetHandlers) {
                int id = entry.getIntKey();
                String baseClassName = entry.getValue().getName();
                baseClassName = baseClassName.substring(baseClassName.lastIndexOf('.') + 1);
                int underscoreIndex = baseClassName.indexOf('_');
                if (underscoreIndex >= 0) {
                    baseClassName = baseClassName.substring(0, underscoreIndex);
                }
                baseClassName = baseClassName.replace("$", "").replace(toReplace, "");
                baseClassName = prefix + baseClassName;
                String className = "net.earthcomputer.multiconnect.packets." + baseClassName;
                Class<?> clazz;
                try {
                    clazz = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    LOGGER.error("Could not find packet class " + className);
                    continue;
                }
                if (clazz.isInterface()) {
                    className = "net.earthcomputer.multiconnect.packets.latest." + baseClassName + "_Latest";
                    try {
                        Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        LOGGER.error("Could not find packet class " + className);
                        continue;
                    }
                }
                pw.println(id + " " + className);
            }
        }
    }


    public static void onDebugKey() {
        handlePacketDump(
                MinecraftClient.getInstance().getNetworkHandler(),
                "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",
                true
        );
    }

    public static String dfuToString(Object dfuType) {
        if (dfuType == null) {
            return "null";
        }
        if (dfuType instanceof Iterable<?> collection) {
            StringBuilder sb = new StringBuilder("[");
            boolean appended = false;
            for (Object o : collection) {
                if (appended) {
                    sb.append(",\n");
                } else {
                    sb.append("\n");
                }
                appended = true;
                sb.append(dfuToString(o));
            }
            return sb.toString().replace("\n", "  \n") + "\n]";
        }

        if (dfuType instanceof TypeRewriteRule.Nop) {
            return "Nop";
        }
        if (dfuType instanceof TypeRewriteRule.Seq) {
            return "Seq" + dfuToString(getField(dfuType, "rules"));
        }
        if (dfuType instanceof TypeRewriteRule.OrElse) {
            return "OrElse[" + dfuToString(getField(dfuType, "first")) + ", " + dfuToString(getField(dfuType, "second")) + "]";
        }
        if (dfuType instanceof TypeRewriteRule.All) {
            return "All[" + dfuToString(getField(dfuType, "rule")) + "]";
        }
        if (dfuType instanceof TypeRewriteRule.One) {
            return "One[" + dfuToString(getField(dfuType, "rule")) + "]";
        }
        if (dfuType instanceof TypeRewriteRule.CheckOnce) {
            return "CheckOnce[" + dfuToString(getField(dfuType, "rule")) + "]";
        }
        if (dfuType instanceof TypeRewriteRule.Everywhere) {
            return "Everywhere[" + dfuToString(getField(dfuType, "rule")) + "]";
        }
        if (dfuType instanceof TypeRewriteRule.IfSame<?>) {
            return "IfSame[" + dfuToString(getField(dfuType, "targetType")) + ", " + dfuToString(getField(dfuType, "value")) + "]";
        }

        return dfuType.toString();
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(Object instance, String name) {
        for (Class<?> clazz = instance.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                return (T) field.get(instance);
            } catch (NoSuchFieldException ignored) {
            } catch (ReflectiveOperationException e) {
                Util.throwUnchecked(e);
            }
        }
        throw new IllegalArgumentException("No such field " + name);
    }
}
