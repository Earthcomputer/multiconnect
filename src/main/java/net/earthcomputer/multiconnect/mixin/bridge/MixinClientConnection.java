package net.earthcomputer.multiconnect.mixin.bridge;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.earthcomputer.multiconnect.api.ThreadSafe;
import net.earthcomputer.multiconnect.impl.DebugUtils;
import net.earthcomputer.multiconnect.impl.TestingAPI;
import net.earthcomputer.multiconnect.protocols.generic.MulticonnectClientboundTranslator;
import net.earthcomputer.multiconnect.protocols.generic.MulticonnectServerboundTranslator;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class MixinClientConnection {

    @Shadow private Channel channel;

    @Inject(method = "setState", at = @At("HEAD"))
    private void onSetState(NetworkState state, CallbackInfo ci) {
        if (state == NetworkState.PLAY) {
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
            LogManager.getLogger("multiconnect").error("Unexpectedly disconnected from server!", t);
        }
    }
}
