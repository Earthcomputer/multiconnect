package net.earthcomputer.multiconnect.mixin.connect;

import net.earthcomputer.multiconnect.debug.DebugUtils;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {
    @Inject(method = "setServerBrand", at = @At("HEAD"))
    private void onSetServerBrand(String serverBrand, CallbackInfo ci) {
        DebugUtils.lastServerBrand = serverBrand;
    }
}
