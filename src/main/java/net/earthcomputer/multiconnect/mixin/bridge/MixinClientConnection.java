package net.earthcomputer.multiconnect.mixin.bridge;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.earthcomputer.multiconnect.api.ThreadSafe;
import net.earthcomputer.multiconnect.impl.ConnectionEndedException;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.DebugUtils;
import net.earthcomputer.multiconnect.impl.TestingAPI;
import net.earthcomputer.multiconnect.protocols.generic.CustomPayloadHandler;
import net.earthcomputer.multiconnect.protocols.generic.ICustomPayloadC2SPacket;
import net.earthcomputer.multiconnect.protocols.generic.MulticonnectClientboundTranslator;
import net.earthcomputer.multiconnect.protocols.generic.MulticonnectServerboundTranslator;
import net.earthcomputer.multiconnect.protocols.v1_12_2.CustomPayloadC2SPacket_1_12_2;
import net.minecraft.SharedConstants;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class MixinClientConnection {

    @Shadow private Channel channel;
    @Shadow private PacketListener packetListener;

    @Inject(method = "channelActive", at = @At("HEAD"))
    private static void onConnect(ChannelHandlerContext ctx, CallbackInfo ci) {
        ctx.pipeline().addBefore("encoder", "multiconnect_serverbound_translator", new MulticonnectServerboundTranslator());
        ctx.pipeline().addBefore("decoder", "multiconnect_clientbound_translator", new MulticonnectClientboundTranslator());
    }

    @Inject(method = "exceptionCaught", at = @At("HEAD"))
    @ThreadSafe
    public void onExceptionCaught(ChannelHandlerContext context, Throwable t, CallbackInfo ci) {
        if (DebugUtils.isUnexpectedDisconnect(t) && channel.isOpen()) {
            TestingAPI.onUnexpectedDisconnect(t);
            LogManager.getLogger("multiconnect").error("Unexpectedly disconnected from server!", t);
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"), cancellable = true)
    @ThreadSafe
    private void onSend(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> callback, CallbackInfo ci) {
        if (!ConnectionInfo.protocol.preSendPacket(packet)) {
            ci.cancel();
        } else if (ConnectionInfo.protocolVersion == SharedConstants.getProtocolVersion()) {
            // no need to translate or block any packets
        } else {
            boolean canceled;
            try {
                canceled = !ConnectionInfo.protocol.onSendPacket(packet);
            } catch (ConnectionEndedException e) {
                canceled = true;
            }
            if (canceled) {
                ci.cancel();
            } else if (packet instanceof CustomPayloadC2SPacket) {
                if (((ICustomPayloadC2SPacket) packet).multiconnect_isBlocked()) {
                    if (packetListener instanceof ClientPlayNetworkHandler networkHandler) {
                        CustomPayloadHandler.handleServerboundCustomPayload(networkHandler, (CustomPayloadC2SPacket) packet);
                    }
                    ci.cancel();
                }
            } else if (packet instanceof CustomPayloadC2SPacket_1_12_2 customPayload) {
                if (customPayload.isBlocked()) {
                    if (packetListener instanceof ClientPlayNetworkHandler networkHandler) {
                        CustomPayloadHandler.handleServerboundCustomPayload(networkHandler, customPayload);
                    }
                    ci.cancel();
                }
            }
        }
    }

}
