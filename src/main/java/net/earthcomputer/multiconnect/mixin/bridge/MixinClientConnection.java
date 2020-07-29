package net.earthcomputer.multiconnect.mixin.bridge;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.concurrent.GenericFutureListener;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.generic.CustomPayloadHandler;
import net.earthcomputer.multiconnect.protocols.generic.ICustomPayloadC2SPacket;
import net.earthcomputer.multiconnect.protocols.v1_12_2.CustomPayloadC2SPacket_1_12_2;
import net.minecraft.SharedConstants;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketEncoderException;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class MixinClientConnection {

    @Shadow private Channel channel;

    @Inject(method = "exceptionCaught", at = @At("HEAD"))
    public void onExceptionCaught(ChannelHandlerContext context, Throwable t, CallbackInfo ci) {
        if (t instanceof DecoderException) {
            ConnectionInfo.resourceReloadLock.readLock().unlock();
        }
        if (!(t instanceof PacketEncoderException) && !(t instanceof TimeoutException) && channel.isOpen()) {
            LogManager.getLogger("multiconnect").error("Unexpectedly disconnected from server!", t);
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"), cancellable = true)
    public void onSend(Packet<?> packet, GenericFutureListener<?> listener, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion == SharedConstants.getGameVersion().getProtocolVersion()) {
            return;
        }
        if (!ConnectionInfo.protocol.onSendPacket(packet)) {
            ci.cancel();
        } else if (packet instanceof CustomPayloadC2SPacket) {
            ICustomPayloadC2SPacket customPayload = (ICustomPayloadC2SPacket) packet;
            if (customPayload.multiconnect_isBlocked()) {
                CustomPayloadHandler.handleServerboundCustomPayload(customPayload);
                ci.cancel();
            }
        } else if (packet instanceof CustomPayloadC2SPacket_1_12_2) {
            CustomPayloadC2SPacket_1_12_2 customPayload = (CustomPayloadC2SPacket_1_12_2) packet;
            if (customPayload.isBlocked()) {
                CustomPayloadHandler.handleServerboundCustomPayload(customPayload);
                ci.cancel();
            }
        }
    }

}
