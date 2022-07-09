package net.earthcomputer.multiconnect.mixin.connect;

import net.earthcomputer.multiconnect.debug.DebugUtils;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Inject(method = "setServerBrand", at = @At("HEAD"))
    private void onSetServerBrand(String serverBrand, CallbackInfo ci) {
        DebugUtils.lastServerBrand = serverBrand;
    }
}
