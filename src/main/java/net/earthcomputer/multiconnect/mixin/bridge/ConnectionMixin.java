package net.earthcomputer.multiconnect.mixin.bridge;

import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class ConnectionMixin {
    @Shadow private Channel channel;

    @Inject(method = {"setEncryptionKey", "setupCompression"}, at = @At("RETURN"))
    private void postPipelineModifiers(CallbackInfo ci) {
        // reinstall transformers to make sure they're in the right order compared to the decoders
        if (channel.pipeline().context("multiconnect_serverbound_translator") != null) {
            var translator = channel.pipeline().remove("multiconnect_serverbound_translator");
            channel.pipeline().addBefore("encoder", "multiconnect_serverbound_translator", translator);
        }
        if (channel.pipeline().context("multiconnect_clientbound_translator") != null) {
            var translator = channel.pipeline().remove("multiconnect_clientbound_translator");
            channel.pipeline().addBefore("decoder", "multiconnect_clientbound_translator", translator);
        }
    }
}
