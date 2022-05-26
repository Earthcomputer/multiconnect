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
            } else {
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
                pw.println("id name oldName");
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
