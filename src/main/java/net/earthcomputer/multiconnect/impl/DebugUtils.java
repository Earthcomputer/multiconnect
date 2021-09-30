package net.earthcomputer.multiconnect.impl;

import com.google.common.collect.ImmutableMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.TimeoutException;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.api.MultiConnectAPI;
import net.earthcomputer.multiconnect.api.ThreadSafe;
import net.earthcomputer.multiconnect.connect.ConnectionMode;
import net.earthcomputer.multiconnect.mixin.connect.ClientConnectionAccessor;
import net.earthcomputer.multiconnect.mixin.connect.DecoderHandlerAccessor;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.generic.AbstractProtocol;
import net.earthcomputer.multiconnect.protocols.generic.ChunkDataTranslator;
import net.earthcomputer.multiconnect.mixin.bridge.ChunkDataPacketAccessor;
import net.earthcomputer.multiconnect.protocols.generic.IIdList;
import net.earthcomputer.multiconnect.protocols.generic.INetworkState;
import net.earthcomputer.multiconnect.protocols.generic.IPacketHandler;
import net.earthcomputer.multiconnect.protocols.generic.IUserDataHolder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.network.DecoderHandler;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketEncoderException;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.state.property.Property;
import net.minecraft.text.BaseText;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.DynamicRegistryManager;
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
import java.util.Arrays;
import java.util.Base64;
import java.util.BitSet;
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

    @SuppressWarnings("unchecked")
    public static void dumpBlockStates() {
        for (int id : ((IIdList<BlockState>) Block.STATE_IDS).multiconnect_ids()) {
            BlockState state = Block.STATE_IDS.get(id);
            assert state != null;
            StringBuilder sb = new StringBuilder().append(id).append(": ").append(Registry.BLOCK.getId(state.getBlock()));
            if (!state.getEntries().isEmpty()) {
                sb.append("[")
                        .append(state.getEntries().entrySet().stream()
                                .sorted(Comparator.comparing(entry -> entry.getKey().getName()))
                                .map(entry -> entry.getKey().getName() + "=" + Util.getValueAsString(entry.getKey(), entry.getValue()))
                                .collect(Collectors.joining(",")))
                        .append("]");
            }
            System.out.println(sb);
        }
    }

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
            MinecraftClient.getInstance().openScreen(parentScreen);
        }, parentScreen.getTitle(), new TranslatableText("multiconnect.rareBug.screen"));
    }

    @ThreadSafe
    public static boolean isUnexpectedDisconnect(Throwable t) {
        return !(t instanceof PacketEncoderException) && !(t instanceof TimeoutException);
    }

    public static void logPacketDisconnectError(byte[] data, String... extraLines) {
        LOGGER.error("!!!!!!!! Unexpected disconnect, please upload this error to " + MULTICONNECT_ISSUES_BASE_URL + " !!!!!!!!");
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
        channel.pipeline().context("decoder").fireChannelRead(Unpooled.wrappedBuffer(bytes));
    }

    public static void handleChunkDataDump(ClientPlayNetworkHandler networkHandler, String base64, boolean compressed, int x, int z, BitSet verticalStripBitmask) {
        byte[] data = decode(base64, compressed);
        if (data == null) {
            return;
        }
        LOGGER.info("Artificially handling chunk data of length {} at {}, {}", data.length, x, z);
        ClientWorld world = networkHandler.getWorld();
        DynamicRegistryManager registryManager = networkHandler.getRegistryManager();
        ChunkDataS2CPacket packet = Utils.createEmptyChunkDataPacket(x, z, world, registryManager);
        ChunkDataPacketAccessor accessor = (ChunkDataPacketAccessor) packet;
        ((IUserDataHolder) accessor).multiconnect_setUserData(ChunkDataTranslator.DATA_TRANSLATED_KEY, false);
        accessor.setData(data);
        accessor.setVerticalStripBitmask(verticalStripBitmask);
        ChunkDataTranslator.submit(packet);
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
    private static <T> void dumpRegistries() throws IOException {
        ConnectionMode connectionMode = ConnectionMode.byValue(ConnectionInfo.protocolVersion);
        File dir = new File("../data/" + connectionMode.getName());
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        dumpPackets(connectionMode, new File(dir, "spackets.csv"), NetworkSide.CLIENTBOUND, "SPacket", "S2CPacket");
        dumpPackets(connectionMode, new File(dir, "cpackets.csv"), NetworkSide.SERVERBOUND, "CPacket", "C2SPacket");
        for (Registries registries : Registries.values()) {
            Stream<Triple<Integer, String, String>> entries;
            if (registries == Registries.BLOCK_STATE) {
                entries = StreamSupport.stream(Block.STATE_IDS.spliterator(), false)
                        .filter(it -> MultiConnectAPI.instance().doesServerKnow(Registry.BLOCK, it.getBlock()))
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
                        .filter(it -> MultiConnectAPI.instance().doesServerKnow(registry, it))
                        .map(it -> {
                            Identifier unmodifiedName = Utils.getUnmodifiedName(registry, it);
                            Identifier oldName = registry.getId(it);
                            return Triple.of(registry.getRawId(it), unmodifiedName == null ? "null" : unmodifiedName.getPath(), oldName == null ? "null" : oldName.getPath());
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
        Identifier unmodifiedName = Utils.getUnmodifiedName(Registry.BLOCK, state.getBlock());
        String result = unmodifiedName == null ? "null" : unmodifiedName.getPath();
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
    private static void dumpPackets(ConnectionMode connectionMode, File output, NetworkSide side, String prefix, String toReplace) throws IOException {
        var packetHandlers = ((INetworkState) (Object) NetworkState.PLAY).getPacketHandlers();
        try (PrintWriter pw = new PrintWriter(new FileWriter(output))) {
            pw.println("id clazz");
            int id = 0;
            for (var handler : ((IPacketHandler<?>) packetHandlers.get(side)).multiconnect_values()) {
                String baseClassName = handler.getPacketClass().getName();
                boolean requireSubversion = baseClassName.startsWith("net.earthcomputer.");
                baseClassName = baseClassName.substring(baseClassName.lastIndexOf('.') + 1);
                int underscoreIndex = baseClassName.indexOf('_');
                if (underscoreIndex >= 0) {
                    baseClassName = baseClassName.substring(0, underscoreIndex);
                }
                baseClassName = baseClassName.replace("$", "").replace(toReplace, "");
                baseClassName = prefix + baseClassName;
                List<String> lowerVersions =
                        Arrays.stream(ConnectionMode.protocolValues()).takeWhile(it -> it.ordinal() <= connectionMode.ordinal()).map(ConnectionMode::getName).toList();
                String className = "net.earthcomputer.multiconnect.packets." + baseClassName;
                boolean hadSubverson = false;
                for (String lowerVersion : lowerVersions) {
                    lowerVersion = lowerVersion.replace('.', '_');
                    String newClassName = "net.earthcomputer.multiconnect.packets.v" + lowerVersion + "." + baseClassName + "_" + lowerVersion;
                    try {
                        Class.forName(newClassName);
                        className = newClassName;
                        hadSubverson = true;
                    } catch (ClassNotFoundException ignore) {}
                }
                if (requireSubversion && !hadSubverson) {
                    System.err.println("Could not find subversion packet for " + handler.getPacketClass());
                }
                pw.println(id + " " + className);
                id++;
            }
        }
    }

    public static void onDebugKey() {
        try {
            dumpRegistries();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class DebugDecoderHandler extends DecoderHandler {
        private final DecoderHandlerAccessor delegate;

        public DebugDecoderHandler(DecoderHandler delegate) {
            this((DecoderHandlerAccessor) delegate);
        }

        private DebugDecoderHandler(DecoderHandlerAccessor delegate) {
            super(delegate.getSide());
            this.delegate = delegate;
        }

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            try {
                delegate.callDecode(ctx, in, out);
            } catch (Throwable t) {
                if (isUnexpectedDisconnect(t)) {
                    logPacketDisconnectError(in.array());
                }
                throw t;
            }
        }
    }
}
