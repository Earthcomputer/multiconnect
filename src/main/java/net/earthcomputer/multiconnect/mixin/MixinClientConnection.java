package net.earthcomputer.multiconnect.mixin;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.concurrent.GenericFutureListener;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketEncoderException;
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
        if (!(t instanceof PacketEncoderException) && !(t instanceof TimeoutException) && channel.isOpen()) {
            LogManager.getLogger("assets/multiconnect").error("Unexpectedly disconnected from server!", t);
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"), cancellable = true)
    public void onSend(Packet<?> packet, GenericFutureListener listener, CallbackInfo ci) {
        if (!ConnectionInfo.protocol.onSendPacket(packet))
            ci.cancel();
    }

}
