package net.earthcomputer.multiconnect.mixin.debug;

import io.netty.channel.Channel;
import net.earthcomputer.multiconnect.debug.PacketRecorder;
import net.earthcomputer.multiconnect.debug.PacketReplay;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ConnectionMixin {
    @Shadow private Channel channel;

    @Inject(method = "setProtocol", at = @At("HEAD"))
    private void onSetProtocol(ConnectionProtocol protocol, CallbackInfo ci) {
        PacketRecorder.onSetConnectionProtocol(protocol);
    }

    @Inject(method = {"setEncryptionKey", "setupCompression"}, at = @At("HEAD"), cancellable = true)
    private void cancelPipelineModifiers(CallbackInfo ci) {
        if (PacketReplay.isReplaying()) {
            ci.cancel();
        }
    }

    @Inject(method = {"setEncryptionKey", "setupCompression"}, at = @At("RETURN"))
    private void postPipelineModifiers(CallbackInfo ci) {
        // reinstall the packet logger to make sure the pipeline is in the right order
        PacketRecorder.install(channel);
    }
}
