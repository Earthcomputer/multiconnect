package net.earthcomputer.multiconnect.mixin.connect;

import net.earthcomputer.multiconnect.protocols.generic.MulticonnectAddedRegistryEntries;
import net.minecraft.server.Bootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bootstrap.class)
public class BootstrapMixin {
    @Inject(method = "bootStrap", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;freezeBuiltins()V"))
    private static void preFreezeRegistries(CallbackInfo ci) {
        MulticonnectAddedRegistryEntries.register();
    }
}
