package net.earthcomputer.multiconnect.protocols.generic;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.DebugUtils;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class MulticonnectClientboundTranslator extends ByteToMessageDecoder {
    private static final Logger LOGGER = LogManager.getLogger();

    @SuppressWarnings("unchecked")
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        ClientConnection clientConnection = (ClientConnection) ctx.pipeline().context("packet_handler").handler();
        PacketListener packetListener = clientConnection.getPacketListener();
        ClientPlayNetworkHandler networkHandler;
        if (packetListener instanceof ClientPlayNetworkHandler) {
            networkHandler = (ClientPlayNetworkHandler) packetListener;
        } else {
            networkHandler = null;
        }
        TypedMap userData = new TypedMap();

        List<ByteBuf> outBufs = (List<ByteBuf>) (List<?>) out;
        try {
            PacketSystem.Internals.translateSPacket(ConnectionInfo.protocolVersion, in, outBufs, networkHandler, userData);
        } catch (Throwable e) {
            DebugUtils.logPacketError(in, "Direction: inbound");
            for (ByteBuf buf : outBufs) {
                buf.release();
            }
            // consume all the input
            in.readerIndex(in.readerIndex() + in.readableBytes());
            if (DebugUtils.IGNORE_ERRORS) {
                LOGGER.warn("Ignoring error in packet");
                e.printStackTrace();
                outBufs.clear();
            } else {
                throw e;
            }
        }

        if (DebugUtils.STORE_BUFS_FOR_HANDLER) {
            userData.put(DebugUtils.STORED_BUF, DebugUtils.getBufData(in));
        }

        for (ByteBuf outBuf : outBufs) {
            PacketSystem.Internals.setUserData(outBuf, userData);
        }
    }
}
