package net.earthcomputer.multiconnect.mixin.debug;

import io.netty.channel.Channel;
import net.earthcomputer.multiconnect.debug.PacketRecorder;
import net.earthcomputer.multiconnect.debug.PacketReplay;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class MixinClientConnection {
    @Shadow private Channel channel;

    @Inject(method = "setState", at = @At("HEAD"))
    private void onSetState(NetworkState state, CallbackInfo ci) {
        PacketRecorder.onSetNetworkState(state);
    }

    @Inject(method = {"setupEncryption", "setCompressionThreshold"}, at = @At("HEAD"), cancellable = true)
    private void cancelPipelineModifiers(CallbackInfo ci) {
        if (PacketReplay.isReplaying()) {
            ci.cancel();
        }
    }

    @Inject(method = {"setupEncryption", "setCompressionThreshold"}, at = @At("RETURN"))
    private void postPipelineModifiers(CallbackInfo ci) {
        // reinstall the packet logger to make sure the pipeline is in the right order
        PacketRecorder.install(channel);
    }
}
