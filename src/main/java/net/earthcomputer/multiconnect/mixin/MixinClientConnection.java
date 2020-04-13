package net.earthcomputer.multiconnect.mixin;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.concurrent.GenericFutureListener;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.SkipableEncoderException;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class MixinClientConnection {

    @Shadow private Channel channel;

    @Inject(method = "exceptionCaught", at = @At("HEAD"))
    public void onExceptionCaught(ChannelHandlerContext context, Throwable t, CallbackInfo ci) {
        if (t instanceof DecoderException) {
            ConnectionInfo.resourceReloadLock.readLock().unlock();
        }
        if (!(t instanceof SkipableEncoderException) && !(t instanceof TimeoutException) && channel.isOpen()) {
            LogManager.getLogger("multiconnect").error("Unexpectedly disconnected from server!", t);
        }
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/IPacket;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"), cancellable = true)
    public void onSend(IPacket<?> packet, GenericFutureListener listener, CallbackInfo ci) {
        if (!ConnectionInfo.protocol.onSendPacket(packet))
            ci.cancel();
    }

}
