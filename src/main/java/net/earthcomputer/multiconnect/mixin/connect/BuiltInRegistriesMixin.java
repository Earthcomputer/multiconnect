package net.earthcomputer.multiconnect.mixin.connect;

import net.earthcomputer.multiconnect.protocols.generic.MulticonnectAddedRegistryEntries;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltInRegistries.class)
public class BuiltInRegistriesMixin {
    @Inject(method = "createContents()V", at = @At("RETURN"))
    private static void preFreezeRegistries(CallbackInfo ci) {
        MulticonnectAddedRegistryEntries.register();
    }
}
