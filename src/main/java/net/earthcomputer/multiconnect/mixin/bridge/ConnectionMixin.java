package net.earthcomputer.multiconnect.mixin.bridge;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.earthcomputer.multiconnect.api.ThreadSafe;
import net.earthcomputer.multiconnect.debug.DebugUtils;
import net.earthcomputer.multiconnect.debug.PacketRecorder;
import net.earthcomputer.multiconnect.debug.TestingAPI;
import net.earthcomputer.multiconnect.protocols.generic.CustomPayloadHandler;
import net.earthcomputer.multiconnect.protocols.generic.MulticonnectClientboundTranslator;
import net.earthcomputer.multiconnect.protocols.generic.MulticonnectServerboundTranslator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class ConnectionMixin {
    @Shadow @Final private static Logger LOGGER;
    @Shadow private Channel channel;
    @Shadow private PacketListener packetListener;

    @Inject(method = "setProtocol", at = @At("HEAD"))
    private void onSetProtocol(ConnectionProtocol protocol, CallbackInfo ci) {
        // Singleplayer doesnt include encoding
        boolean enableTranslation = !Minecraft.getInstance().hasSingleplayerServer() && !DebugUtils.SKIP_TRANSLATION;

        if (enableTranslation) {
            PacketRecorder.install(channel);
        }

        if (channel.pipeline().context("multiconnect_serverbound_translator") != null) {
            channel.pipeline().remove("multiconnect_serverbound_translator");
        }
        if (channel.pipeline().context("multiconnect_clientbound_translator") != null) {
            channel.pipeline().remove("multiconnect_clientbound_translator");
        }

        if (protocol == ConnectionProtocol.PLAY && enableTranslation) {
            String serverboundBefore = channel.pipeline().context("multiconnect_serverbound_logger") != null ? "multiconnect_serverbound_logger" : "encoder";
            channel.pipeline().addBefore(serverboundBefore, "multiconnect_serverbound_translator", new MulticonnectServerboundTranslator());
            channel.pipeline().addBefore("decoder", "multiconnect_clientbound_translator", new MulticonnectClientboundTranslator());
        }
    }

    @Inject(method = "exceptionCaught", at = @At("HEAD"))
    @ThreadSafe
    public void onExceptionCaught(ChannelHandlerContext context, Throwable t, CallbackInfo ci) {
        if (DebugUtils.isUnexpectedDisconnect(t) && channel.isOpen()) {
            TestingAPI.onUnexpectedDisconnect(t);
            if (context.channel().attr(DebugUtils.NETTY_HAS_HANDLED_ERROR).get() != Boolean.TRUE) {
                if (!DebugUtils.STORE_BUFS_FOR_HANDLER) {
                    LOGGER.error("Note: to get a more complete error, run with JVM argument -Dmulticonnect.storeBufsForHandler=true");
                } else {
                    byte[] buf = context.channel().attr(DebugUtils.NETTY_STORED_BUF).get();
                    if (buf != null) {
                        DebugUtils.logPacketError(buf);
                    }
                }
            }
            LOGGER.error("Unexpectedly disconnected from server!", t);
        }
    }

    // TODO: move this to the network layer
    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"), cancellable = true)
    public void onSend(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> callback, CallbackInfo ci) {
        if (packet instanceof ServerboundCustomPayloadPacket customPayload
                && !customPayload.getIdentifier().equals(ServerboundCustomPayloadPacket.BRAND)) {
            if (packetListener instanceof ClientPacketListener networkHandler) {
                FriendlyByteBuf dataBuf = customPayload.getData();
                byte[] data = new byte[dataBuf.readableBytes()];
                dataBuf.readBytes(data);
                CustomPayloadHandler.handleServerboundCustomPayload(networkHandler, customPayload.getIdentifier(), data);
            }
            ci.cancel();
        }
    }
}
