package net.earthcomputer.multiconnect.protocols.generic;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;

import java.util.ArrayList;
import java.util.List;

public class MulticonnectServerboundTranslator extends ChannelOutboundHandlerAdapter {
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
            PacketSystem.Internals.translateCPacket(ConnectionInfo.protocolVersion, untranslated, bufs, networkHandler, userData);
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
