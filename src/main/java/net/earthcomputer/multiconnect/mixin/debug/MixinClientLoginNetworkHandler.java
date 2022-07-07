package net.earthcomputer.multiconnect.mixin.debug;

import net.earthcomputer.multiconnect.debug.PacketReplay;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLoginNetworkHandler.class)
public class MixinClientLoginNetworkHandler {
    @Inject(method = "joinServerSession", at = @At("HEAD"), cancellable = true)
    private void dontAuthenticate(CallbackInfoReturnable<Text> ci) {
        if (PacketReplay.isReplaying()) {
            ci.setReturnValue(null);
        }
    }
}
