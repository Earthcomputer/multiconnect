package net.earthcomputer.multiconnect.mixin.bridge;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.earthcomputer.multiconnect.api.ThreadSafe;
import net.earthcomputer.multiconnect.impl.DebugUtils;
import net.earthcomputer.multiconnect.impl.TestingAPI;
import net.earthcomputer.multiconnect.protocols.generic.CustomPayloadHandler;
import net.earthcomputer.multiconnect.protocols.generic.MulticonnectClientboundTranslator;
import net.earthcomputer.multiconnect.protocols.generic.MulticonnectServerboundTranslator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class MixinClientConnection {
    @Shadow @Final private static Logger LOGGER;
    @Shadow private Channel channel;
    @Shadow private PacketListener packetListener;

    @Inject(method = "setState", at = @At("HEAD"))
    private void onSetState(NetworkState state, CallbackInfo ci) {
        // Singleplayer doesnt include encoding
        if (state == NetworkState.PLAY && !MinecraftClient.getInstance().isIntegratedServerRunning() && !DebugUtils.SKIP_TRANSLATION) {
            channel.pipeline().addBefore("encoder", "multiconnect_serverbound_translator", new MulticonnectServerboundTranslator());
            channel.pipeline().addBefore("decoder", "multiconnect_clientbound_translator", new MulticonnectClientboundTranslator());
        } else {
            if (channel.pipeline().context("multiconnect_serverbound_translator") != null) {
                channel.pipeline().remove("multiconnect_serverbound_translator");
            }
            if (channel.pipeline().context("multiconnect_clientbound_translator") != null) {
                channel.pipeline().remove("multiconnect_clientbound_translator");
            }
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
    @Inject(method = "send(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"), cancellable = true)
    public void onSend(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> callback, CallbackInfo ci) {
        if (packet instanceof CustomPayloadC2SPacket customPayload
                && !customPayload.getChannel().equals(CustomPayloadC2SPacket.BRAND)) {
            if (packetListener instanceof ClientPlayNetworkHandler networkHandler) {
                PacketByteBuf dataBuf = customPayload.getData();
                byte[] data = new byte[dataBuf.readableBytes()];
                dataBuf.readBytes(data);
                CustomPayloadHandler.handleServerboundCustomPayload(networkHandler, customPayload.getChannel(), data);
            }
            ci.cancel();
        }
    }
}
