package net.earthcomputer.multiconnect.mixin.bridge;

import io.netty.channel.Channel;
import net.earthcomputer.multiconnect.impl.Multiconnect;
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
        Multiconnect.translator.postPipelineModifiers(channel);
    }
}
