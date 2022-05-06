package net.earthcomputer.multiconnect.impl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.handler.timeout.TimeoutException;
import net.earthcomputer.multiconnect.api.ThreadSafe;
import net.earthcomputer.multiconnect.connect.ConnectionMode;
import net.earthcomputer.multiconnect.mixin.connect.ClientConnectionAccessor;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.generic.AbstractProtocol;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.network.PacketEncoderException;
import net.minecraft.text.BaseText;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
        try {
            ByteBuf buf = Unpooled.wrappedBuffer(bytes);
            buf.readerIndex(0);
            if (channel.eventLoop().inEventLoop()) {
                ((ChannelInboundHandler) context.handler()).channelRead(context, buf);
            } else {
                channel.eventLoop().execute(() -> {
                    try {
                        ((ChannelInboundHandler) context.handler()).channelRead(context, buf);
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

    public static void onDebugKey() {
        handlePacketDump(
                MinecraftClient.getInstance().getNetworkHandler(),
                "H4sIAAAAAAAA/1NhYGBgYTigcmraiys2Qg4BBxhA4IA7t6FxsDgbAxKwk5w5C0gZOQEA+YjcUTMAAAA=",
                true
        );
    }
}
