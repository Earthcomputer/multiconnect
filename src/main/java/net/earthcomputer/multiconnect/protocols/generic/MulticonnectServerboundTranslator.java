package net.earthcomputer.multiconnect.protocols.generic;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.debug.DebugUtils;
import net.earthcomputer.multiconnect.impl.PacketIntrinsics;
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

        DebugUtils.wrapInErrorHandler(ctx, untranslated, "outbound", () -> {
            var result = PacketSystem.Internals.translateCPacket(ConnectionInfo.protocolVersion, untranslated);
            ByteBuf inCopy = untranslated.copy(0, untranslated.readerIndex() + untranslated.readableBytes());
            inCopy.readerIndex(untranslated.readerIndex());
            untranslated.readerIndex(untranslated.readerIndex() + untranslated.readableBytes());
            PacketSystem.Internals.submitTranslationTask(result.readDependencies(), result.writeDependencies(), () -> {
                DebugUtils.wrapInErrorHandler(ctx, inCopy, "outbound", () -> {
                    result.sender().send(inCopy, bufs, networkHandler, PacketSystem.Internals.getGlobalData(), userData);
                    // don't need user data in the serverbound direction
                });
            }, () -> {
                DebugUtils.wrapInErrorHandler(ctx, inCopy, "outbound", () -> {
                    PacketIntrinsics.sendRawToServer(networkHandler, bufs);
                });
            }, false);
        });
    }
}
