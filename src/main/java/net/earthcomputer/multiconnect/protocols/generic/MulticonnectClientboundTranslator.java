package net.earthcomputer.multiconnect.protocols.generic;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;

import java.util.List;

public class MulticonnectClientboundTranslator extends ByteToMessageDecoder {
    @SuppressWarnings("unchecked")
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        ClientConnection clientConnection = (ClientConnection) ctx.pipeline().context("packet_handler");
        PacketListener packetListener = clientConnection.getPacketListener();
        ClientPlayNetworkHandler networkHandler;
        if (packetListener instanceof ClientPlayNetworkHandler) {
            networkHandler = (ClientPlayNetworkHandler) packetListener;
        } else {
            networkHandler = null;
        }
        TypedMap userData = new TypedMap();

        List<ByteBuf> outBufs = (List<ByteBuf>) (List<?>) out;
        PacketSystem.Internals.translateSPacket(ConnectionInfo.protocolVersion, in, outBufs, networkHandler, userData);
        for (ByteBuf outBuf : outBufs) {
            PacketSystem.Internals.setUserData(outBuf, userData);
        }
    }
}
