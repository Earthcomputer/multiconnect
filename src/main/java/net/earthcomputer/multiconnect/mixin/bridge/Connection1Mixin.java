package net.earthcomputer.multiconnect.mixin.bridge;

import io.netty.channel.Channel;
import net.earthcomputer.multiconnect.impl.Multiconnect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.network.Connection$1")
public class Connection1Mixin {
    @Inject(method = "initChannel", at = @At("RETURN"))
    private void onInitChannel(Channel channel, CallbackInfo ci) {
        Multiconnect.translator.inject(channel);
    }
}
