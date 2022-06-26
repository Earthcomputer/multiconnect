package net.earthcomputer.multiconnect.debug;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import net.earthcomputer.multiconnect.connect.ConnectionMode;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.DecoderHandler;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketEncoder;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

public final class PacketReplay {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static DataInputStream stream;
    private static BufferedWriter htmlWriter;
    private static ClientConnection connection;
    private static Channel channel;
    private static int packetCount;

    private PacketReplay() {
    }

    public static boolean isReplaying() {
        return stream != null;
    }

    public static void onPacketDeserialized(Object packet, boolean clientbound) {
        if (stream == null) {
            return;
        }

        String clientboundStr = clientbound ? "Clientbound" : "Serverbound";
        try {
            htmlWriter.write("<h2>" + clientboundStr + " Packet</h2>\n");
            htmlWriter.write(PacketVisualizer.visualize(packet, clientbound, packetCount++) + "\n");
        } catch (IOException e) {
            LOGGER.error("Error writing to HTML file", e);
        }
    }

    public static void start() {
        if (!loadFile()) {
            return;
        }
        startReplayServer();
    }

    public static void tick() {
        if (stream == null) {
            return;
        }
        while (readPacketLogEntry())
            ;
    }

    private static boolean readPacketLogEntry() {
        try {
            int type;
            try {
                type = stream.readUnsignedByte();
            } catch (EOFException e) {
                LOGGER.warn("Reached the end of the packet log without finding a disconnect packet");
                MinecraftClient.getInstance().disconnect();
                return false;
            }

            switch (type) {
                case PacketRecorder.CLIENTBOUND_PACKET, PacketRecorder.SERVERBOUND_PACKET -> {
                    int length = stream.readInt();
                    byte[] data = new byte[length];
                    stream.readFully(data);
                    ByteBuf buf = channel.alloc().buffer(data.length);
                    buf.writeBytes(data);
                    if (type == PacketRecorder.CLIENTBOUND_PACKET) {
                        channel.pipeline().fireChannelRead(buf);
                    } else {
                        channel.pipeline().context("decoder").writeAndFlush(buf);
                    }
                    return true;
                }
                case PacketRecorder.PLAYER_POSITION -> {
                    BlockPos pos = BlockPos.fromLong(stream.readLong());
                    short packedFractional = stream.readShort();
                    int dx = packedFractional & 15;
                    int dy = (packedFractional >> 8) & 15;
                    int dz = (packedFractional >> 4) & 15;
                    double x = pos.getX() + dx / 16.0;
                    double y = pos.getY() + dy / 16.0;
                    double z = pos.getZ() + dz / 16.0;
                    float yaw = stream.readUnsignedByte() * 360.0F / 256.0F;
                    float pitch = stream.readUnsignedByte() * 360.0F / 256.0F;
                    ClientPlayerEntity player = MinecraftClient.getInstance().player;
                    assert player != null;
                    player.refreshPositionAndAngles(x, y, z, yaw, pitch);
                    return true;
                }
                case PacketRecorder.TICK -> {
                    return false;
                }
                case PacketRecorder.NETWORK_STATE -> {
                    int stateId = stream.readByte();
                    NetworkState state = NetworkState.byId(stateId);
                    if (state == null) {
                        LOGGER.warn("Invalid network state: {}", stateId);
                        return false;
                    }
                    connection.setState(state);
                    return true;
                }
                case PacketRecorder.DISCONNECTED -> {
                    LOGGER.info("End of packet replay");
                    MinecraftClient.getInstance().disconnect();
                    return false;
                }
                default -> {
                    LOGGER.warn("Unknown packet log entry type {}", type);
                    MinecraftClient.getInstance().disconnect();
                    return false;
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read packet log entry", e);
            MinecraftClient.getInstance().disconnect();
            return false;
        }
    }

    public static void stop() {
        if (stream == null) {
            return;
        }

        channel = null;
        try {
            stream.close();
        } catch (IOException e) {
            LOGGER.error("Error closing packet replay file", e);
        }
        stream = null;

        try {
            htmlWriter.write("<script>\n");
            htmlWriter.write(PacketVisualizer.SCRIPT_EPILOGUE + "\n");
            htmlWriter.write("</script>\n");
            htmlWriter.write("</body>\n");
            htmlWriter.write("</html>\n");
            htmlWriter.close();
        } catch (IOException e) {
            LOGGER.error("Error closing packet replay HTML file", e);
        }
        htmlWriter = null;

        startHttpServer();

        connection = null;
    }

    private static void fetchFile(HttpRequest request, HttpResponse response, HttpContext context) throws IOException {
        String path;
        try {
            path = new URI(request.getRequestLine().getUri()).getPath();
        } catch (URISyntaxException e) {
            LOGGER.error("Invalid URI: {}", request.getRequestLine().getUri());
            return;
        }
        while (path.startsWith("/")) {
            path = path.substring(1);
        }

        if (path.isEmpty()) {
            path = "replay.html";
        }
        Path file = FabricLoader.getInstance().getConfigDir().resolve("multiconnect").resolve("packet-logs").resolve(path);
        if (Files.isDirectory(file)) {
            file = file.resolve("index.html");
        }
        if (!Files.isRegularFile(file)) {
            response.setStatusCode(404);
            return;
        }
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(file)));
            response.setStatusCode(200);
            response.setEntity(new InputStreamEntity(in, in.available()));
        } catch (IOException e) {
            LOGGER.error("Error reading file", e);
            response.setStatusCode(500);
        }
    }

    private static void startHttpServer() {
        try {
            ServerBootstrap bootstrap = ServerBootstrap.bootstrap();
            bootstrap.registerHandler("*", PacketReplay::fetchFile);
            bootstrap.setListenerPort(8080);
            HttpServer server = bootstrap.create();
            server.start();
            Thread thread = new Thread(() -> {
                Util.getOperatingSystem().open("http://localhost:8080/");
            });
            thread.setDaemon(true);
            thread.start();
            MinecraftClient.getInstance().setScreen(new RunningHttpServerScreen(server));
        } catch (IOException e) {
            LOGGER.error("Error starting HTTP server", e);
        }
    }

    private static boolean loadFile() {
        Path logsDir = FabricLoader.getInstance().getConfigDir().resolve("multiconnect").resolve("packet-logs");
        try {
            stream = new DataInputStream(new GZIPInputStream(new BufferedInputStream(Files.newInputStream(logsDir.resolve("replay.log.gz")))));
        } catch (IOException e) {
            try {
                stream = new DataInputStream(new BufferedInputStream(Files.newInputStream(logsDir.resolve("replay.log"))));
            } catch (IOException e1) {
                LOGGER.error("Failed to open packet replay file", e);
                return false;
            }
        }

        packetCount = 0;
        PacketVisualizer.reset();

        try {
            htmlWriter = Files.newBufferedWriter(logsDir.resolve("replay.html"));
            htmlWriter.write("<!DOCTYPE html>\n");
            htmlWriter.write("<html>\n");
            htmlWriter.write("<head>\n");
            htmlWriter.write("<meta charset=\"UTF-8\">\n");
            htmlWriter.write("<title>Packet Replay</title>\n");
            htmlWriter.write("<style>\n");
            htmlWriter.write(PacketVisualizer.STYLESHEET + "\n");
            htmlWriter.write("</style>\n");
            htmlWriter.write("</head>\n");
            htmlWriter.write("<body>\n");
            htmlWriter.write("<h1>Packet Replay</h1>\n");
        } catch (IOException e) {
            LOGGER.error("Failed to open packet replay HTML file", e);
            return false;
        }

        NbtCompound nbt;
        try {
            nbt = NbtIo.read(stream);
        } catch (IOException e) {
            LOGGER.error("Invalid packet replay file", e);
            return false;
        }

        if (nbt.getInt("version") > PacketRecorder.VERSION) {
            LOGGER.error("Packet replay file has version {} which is newer than the current version {}", nbt.getInt("version"), PacketRecorder.VERSION);
            return false;
        }

        int protocol = nbt.getInt("protocol");
        if (!ConnectionMode.isSupportedProtocol(protocol)) {
            LOGGER.error("Packet replay file has protocol {} which is not supported", protocol);
            return false;
        }
        ConnectionInfo.protocolVersion = protocol;
        ConnectionInfo.protocol = ProtocolRegistry.get(protocol);
        ConnectionInfo.protocol.setup();

        Dynamic<NbtElement> nbtDyn = new Dynamic<>(NbtOps.INSTANCE, nbt);
        Dynamic<JsonElement> jsonDyn = nbtDyn.convert(JsonOps.INSTANCE);
        JsonObject json = jsonDyn.getValue().getAsJsonObject();
        LOGGER.info("Packet replay metadata:");
        for (String line : new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(json).split("\n")) {
            LOGGER.info(line);
        }

        return true;
    }

    private static void startReplayServer() {
        MinecraftClient mc = MinecraftClient.getInstance();

        connection = new ClientConnection(NetworkSide.CLIENTBOUND);
        PacketReplayLoginScreen screen = new PacketReplayLoginScreen(connection);
        mc.setScreen(screen);
        connection.setPacketListener(new ClientLoginNetworkHandler(connection, mc, mc.currentScreen, screen::setStatus));

        channel = new EmbeddedChannel(true, new ChannelInitializer<EmbeddedChannel>() {
            @Override
            protected void initChannel(@NotNull EmbeddedChannel ch) {
                ch.pipeline().addLast("decoder", new DecoderHandler(NetworkSide.CLIENTBOUND));
                ch.pipeline().addLast("encoder", new PacketEncoder(NetworkSide.SERVERBOUND));
                ch.pipeline().addLast("drop_vanilla_packets", new DropVanillaPackets());
                ch.pipeline().addLast("packet_handler", connection);
            }
        });
    }

    private static class DropVanillaPackets extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
            // all serverbound packets sent by the game are dropped here
            // we only want packets from the replay
        }
    }
}
