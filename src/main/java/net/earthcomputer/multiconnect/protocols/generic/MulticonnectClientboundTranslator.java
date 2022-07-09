package net.earthcomputer.multiconnect.protocols.generic;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.debug.DebugUtils;
import net.earthcomputer.multiconnect.impl.PacketIntrinsics;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import java.util.ArrayList;
import java.util.List;

public class MulticonnectClientboundTranslator extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        Connection clientConnection = (Connection) ctx.pipeline().context("packet_handler").handler();
        PacketListener packetListener = clientConnection.getPacketListener();
        ClientPacketListener connection;
        if (packetListener instanceof ClientPacketListener) {
            connection = (ClientPacketListener) packetListener;
        } else {
            throw new AssertionError("Packet listener is not a ClientPacketListener");
        }
        TypedMap userData = new TypedMap();

        ctx.channel().attr(DebugUtils.NETTY_HAS_HANDLED_ERROR).set(Boolean.FALSE);
        if (DebugUtils.STORE_BUFS_FOR_HANDLER) {
            byte[] bufData = DebugUtils.getBufData(in);
            userData.put(DebugUtils.STORED_BUF, bufData);
            ctx.channel().attr(DebugUtils.NETTY_STORED_BUF).set(bufData);
        }

        DebugUtils.wrapInErrorHandler(ctx, in, "inbound", () -> {
            var result = PacketSystem.Internals.translateSPacket(ConnectionInfo.protocolVersion, in);
            ByteBuf inCopy = in.copy(0, in.readerIndex() + in.readableBytes());
            inCopy.readerIndex(in.readerIndex());
            in.readerIndex(in.readerIndex() + in.readableBytes());
            List<ByteBuf> outBufs = new ArrayList<>(1);
            PacketSystem.Internals.submitTranslationTask(result.readDependencies(), result.writeDependencies(), () -> {
                DebugUtils.wrapInErrorHandler(ctx, inCopy, "inbound", () -> {
                    result.sender().send(inCopy, outBufs, connection, PacketSystem.Internals.getGlobalData(), userData);
                    for (ByteBuf outBuf : outBufs) {
                        PacketSystem.Internals.setUserData(outBuf, userData);
                    }
                });
            }, () -> {
                DebugUtils.wrapInErrorHandler(ctx, inCopy, "inbound", () -> {
                    PacketIntrinsics.sendRawToClient(connection, userData, outBufs);
                });
            }, true);
        });
    }
}
