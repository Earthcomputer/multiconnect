package net.earthcomputer.multiconnect.mixin.connect;

import net.earthcomputer.multiconnect.protocols.generic.MulticonnectAddedRegistryEntries;
import net.minecraft.client.gui.screens.LoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoadingOverlay.class)
public class LoadingOverlayMixin {
    @Inject(method = "registerTextures", at = @At("HEAD"))
    private static void onGameInitRenderInitialized(CallbackInfo ci) {
        MulticonnectAddedRegistryEntries.initializeClient();
    }
}
