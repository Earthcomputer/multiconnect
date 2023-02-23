package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeModeTabs.class)
public class CreativeModeTabsMixin {
    private static int protocolValue = Integer.MIN_VALUE;

    @Inject(method = "wouldRebuildSameContents", at = @At("HEAD"), cancellable = true)
    private static void alsoCheckProtocolVersion(FeatureFlagSet featureFlagSet, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        if (protocolValue != ConnectionInfo.protocolVersion) {
            protocolValue = ConnectionInfo.protocolVersion;
            cir.setReturnValue(false);
        }
    }
}
