package net.earthcomputer.multiconnect.protocols.generic;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.DebugUtils;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class MulticonnectServerboundTranslator extends ChannelOutboundHandlerAdapter {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (!(msg instanceof ByteBuf untranslated)) {
            return;
        }

        try {
            ClientConnection clientConnection = (ClientConnection) ctx.pipeline().context("packet_handler").handler();
            PacketListener packetListener = clientConnection.getPacketListener();
            ClientPlayNetworkHandler networkHandler;
            if (packetListener instanceof ClientPlayNetworkHandler) {
                networkHandler = (ClientPlayNetworkHandler) packetListener;
            } else {
                networkHandler = null;
            }
            TypedMap userData = new TypedMap();

            List<ByteBuf> bufs = new ArrayList<>(1);
            try {
                PacketSystem.Internals.translateCPacket(ConnectionInfo.protocolVersion, untranslated, bufs, networkHandler, userData);
            } catch (Throwable e) {
                DebugUtils.logPacketError(untranslated, "Direction: outbound");
                for (ByteBuf buf : bufs) {
                    buf.release();
                }
                // consume all the input
                untranslated.readerIndex(untranslated.readerIndex() + untranslated.readableBytes());
                if (DebugUtils.IGNORE_ERRORS) {
                    LOGGER.warn("Ignoring error in packet");
                    e.printStackTrace();
                    bufs.clear();
                } else {
                    throw e;
                }
            }
            for (ByteBuf translated : bufs) {
                // No need to set the user data here, it's not used anymore
                if (translated.isReadable()) {
                    ctx.write(translated, promise);
                } else {
                    translated.release();
                    ctx.write(Unpooled.EMPTY_BUFFER, promise);
                }
            }
        } finally {
            untranslated.release();
        }
    }
}
