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

    public static void logPacketError(ByteBuf data, String... extraLines) {
        if (data.hasArray()) {
            logPacketError(data.array(), extraLines);
        } else {
            int prevReaderIndex = data.readerIndex();
            data.readerIndex(0);
            byte[] array = new byte[data.readableBytes()];
            data.readBytes(array);
            data.readerIndex(prevReaderIndex);
            logPacketError(array, extraLines);
        }
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
