package net.earthcomputer.multiconnect.debug;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.MulticonnectConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.NetworkState;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public final class PacketRecorder {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int VERSION = 1;
    public static final int CLIENTBOUND_PACKET = 0;
    public static final int SERVERBOUND_PACKET = 1;
    public static final int PLAYER_POSITION = 2;
    public static final int TICK = 3;
    public static final int NETWORK_STATE = 4;
    public static final int DISCONNECTED = 255;
    private static final Object LOCK = new Object();
    private static DataOutputStream stream;
    private static boolean hasThrownError = false;

    private PacketRecorder() {
    }

    public static boolean isEnabled(){
        return (Boolean.getBoolean("multiconnect.enablePacketRecorder") || Boolean.TRUE.equals(MulticonnectConfig.INSTANCE.enablePacketRecorder));
    }

    public static void onConnect() {
        if (!isEnabled()) {
            return;
        }

        synchronized (LOCK) {
            hasThrownError = false;

            ServerInfo server = MinecraftClient.getInstance().getCurrentServerEntry();
            String serverIp = server == null ? "unknown" : server.address;

            try {
                Path dir = FabricLoader.getInstance().getConfigDir().resolve("multiconnect").resolve("packet-logs");
                Files.createDirectories(dir);
                Path logFile = dir.resolve("latest.log");
                zipLogFile(logFile);
                stream = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(logFile)));
            } catch (IOException e) {
                LOGGER.error("Failed to create packet log file", e);
                return;
            }

            NbtCompound header = new NbtCompound();
            header.putInt("version", VERSION);
            header.putString("minecraft-version", SharedConstants.getGameVersion().getId());
            FabricLoader.getInstance().getModContainer("multiconnect").ifPresent(modContainer -> {
                header.putString("multiconnect-version", modContainer.getMetadata().getVersion().getFriendlyString());
            });
            header.putInt("protocol", ConnectionInfo.protocolVersion);
            header.putString("server-ip", serverIp);
            header.putLong("timestamp", Instant.now().toEpochMilli());

            try {
                NbtIo.write(header, stream);
            } catch (IOException e) {
                LOGGER.error("Failed to write packet log header", e);
                try {
                    stream.close();
                } catch (IOException ex) {
                    LOGGER.error("Failed to close packet log file", ex);
                }
                stream = null;
            }
        }
    }

    private static void zipLogFile(Path oldLog) throws IOException {
        NbtCompound nbt;
        try (var in = new DataInputStream(Files.newInputStream(oldLog))) {
            nbt = NbtIo.read(in);
        } catch (IOException e) {
            return;
        }
        String serverIp = URLEncoder.encode(nbt.getString("server-ip"), StandardCharsets.UTF_8);
        long timestamp = nbt.getLong("timestamp");
        LocalDateTime localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        String timeString = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss").format(localTime);
        String filename = String.format("%s-%s.log.gz", serverIp, timeString);
        Path newLog = oldLog.resolveSibling(filename);
        try (var in = Files.newInputStream(oldLog);
             var out = new GZIPOutputStream(Files.newOutputStream(newLog))
        ) {
            IOUtils.copy(in, out);
        }

        LOGGER.info("Packet recording file saved to {}", newLog.toAbsolutePath());

        Files.delete(oldLog);
    }

    public static void onDisconnect() {
        if (!isEnabled()) {
            return;
        }
        synchronized (LOCK) {
            if (stream == null) {
                return;
            }
            try (var s = stream) {
                s.write(DISCONNECTED);
            } catch (IOException e) {
                LOGGER.error("Failed to write packet log footer", e);
            }
            stream = null;
            Path logFile = FabricLoader.getInstance().getConfigDir().resolve("multiconnect").resolve("packet-logs").resolve("latest.log");
            try {
                zipLogFile(logFile);
            } catch (IOException e) {
                LOGGER.error("Failed to zip packet log file", e);
            }
        }
    }

    public static void dumpPacket(ByteBuf buf, boolean clientbound) {
        writePacketLogEntry(() -> {
            stream.writeByte(clientbound ? CLIENTBOUND_PACKET : SERVERBOUND_PACKET);
            stream.writeInt(buf.readableBytes());
            int readerIndex = buf.readerIndex();
            buf.readBytes(stream, buf.readableBytes());
            buf.readerIndex(readerIndex);
        });
    }

    public static void tickMovement(ClientPlayerEntity player) {
        writePacketLogEntry(() -> {
            stream.writeByte(PLAYER_POSITION);
            BlockPos blockPos = player.getBlockPos();
            stream.writeLong(blockPos.asLong());
            int dx = (int) ((player.getX() - blockPos.getX()) * 16) & 15;
            int dy = (int) ((player.getY() - blockPos.getY()) * 16) & 15;
            int dz = (int) ((player.getZ() - blockPos.getZ()) * 16) & 15;
            stream.writeShort((dy << 8) | (dz << 4) | dx);
            stream.writeByte((int) (player.getYaw() * 256 / 360));
            stream.writeByte((int) (player.getPitch() * 256 / 360));
        });
    }

    public static void tick() {
        writePacketLogEntry(() -> stream.writeByte(TICK));
    }

    public static void onSetNetworkState(NetworkState state) {
        writePacketLogEntry(() -> {
            stream.writeByte(NETWORK_STATE);
            stream.writeByte(state.getId());
        });
    }

    private static void writePacketLogEntry(EntryWriter entryWriter) {
        if (!isEnabled()) {
            return;
        }

        synchronized (LOCK) {
            if (stream == null) {
                if (!hasThrownError) {
                    LOGGER.error("Packet logger was not initialized before first packet was received");
                    hasThrownError = true;
                }
                return;
            }

            try {
                entryWriter.write();
                stream.flush();
            } catch (IOException e) {
                LOGGER.error("Failed to write packet log entry", e);
                try {
                    stream.close();
                } catch (IOException ex) {
                    LOGGER.error("Failed to close packet log file", ex);
                }
                stream = null;
            }
        }
    }

    public static void install(Channel channel) {
        if (!(isEnabled()) || stream == null) {
            return;
        }
        if (channel.pipeline().context("multiconnect_clientbound_logger") != null) {
            channel.pipeline().remove("multiconnect_clientbound_logger");
        }
        if (channel.pipeline().context("multiconnect_serverbound_logger") != null) {
            channel.pipeline().remove("multiconnect_serverbound_logger");
        }
        channel.pipeline().addBefore("decoder", "multiconnect_clientbound_logger", new ClientboundPacketLogger());
        channel.pipeline().addBefore("encoder", "multiconnect_serverbound_logger", new ServerboundPacketLogger());
    }

    private static final class ClientboundPacketLogger extends ByteToMessageDecoder {
        @Override
        protected void decode(ChannelHandlerContext context, ByteBuf buf, List<Object> messages) {
            dumpPacket(buf, true);
            messages.add(context.alloc().buffer(buf.readableBytes()).writeBytes(buf));
        }
    }

    private static final class ServerboundPacketLogger extends MessageToByteEncoder<ByteBuf> {
        @Override
        protected void encode(ChannelHandlerContext context, ByteBuf inBuf, ByteBuf outBuf) {
            dumpPacket(inBuf, false);
            outBuf.writeBytes(inBuf);
        }
    }

    @FunctionalInterface
    private interface EntryWriter {
        void write() throws IOException;
    }
}
