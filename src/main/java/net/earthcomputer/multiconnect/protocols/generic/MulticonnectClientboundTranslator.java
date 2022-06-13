package net.earthcomputer.multiconnect.protocols.generic;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.DebugUtils;
import net.earthcomputer.multiconnect.impl.PacketIntrinsics;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;

import java.util.ArrayList;
import java.util.List;

public class MulticonnectClientboundTranslator extends ByteToMessageDecoder {
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

        if (DebugUtils.STORE_BUFS_FOR_HANDLER) {
            byte[] bufData = DebugUtils.getBufData(in);
            userData.put(DebugUtils.STORED_BUF, bufData);
            ctx.channel().attr(DebugUtils.NETTY_STORED_BUF).set(bufData);
        }

        DebugUtils.wrapInErrorHandler(in, "inbound", () -> {
            var result = PacketSystem.Internals.translateSPacket(ConnectionInfo.protocolVersion, in);
            ByteBuf inCopy = in.copy(0, in.readerIndex() + in.readableBytes());
            inCopy.readerIndex(in.readerIndex());
            in.readerIndex(in.readerIndex() + in.readableBytes());
            List<ByteBuf> outBufs = new ArrayList<>(1);
            PacketSystem.Internals.submitTranslationTask(result.readDependencies(), result.writeDependencies(), () -> {
                DebugUtils.wrapInErrorHandler(inCopy, "inbound", () -> {
                    result.sender().send(inCopy, outBufs, networkHandler, PacketSystem.Internals.getGlobalData(), userData);
                    for (ByteBuf outBuf : outBufs) {
                        PacketSystem.Internals.setUserData(outBuf, userData);
                    }
                });
            }, () -> {
                DebugUtils.wrapInErrorHandler(inCopy, "inbound", () -> {
                    PacketIntrinsics.sendRawToClient(networkHandler, userData, outBufs);
                });
            }, true);
        });
    }
}
