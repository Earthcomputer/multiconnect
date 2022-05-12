package net.earthcomputer.multiconnect.mixin.connect;

import net.earthcomputer.multiconnect.protocols.generic.MulticonnectAddedRegistryEntries;
import net.minecraft.client.gui.screen.SplashOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashOverlay.class)
public class MixinSplashOverlay {
    @Inject(method = "init", at = @At("HEAD"))
    private static void onGameInitRenderInitialized(CallbackInfo ci) {
        MulticonnectAddedRegistryEntries.initializeClient();
    }
}
