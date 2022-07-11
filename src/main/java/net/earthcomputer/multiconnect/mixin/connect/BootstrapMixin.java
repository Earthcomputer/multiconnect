package net.earthcomputer.multiconnect.mixin.connect;

import net.earthcomputer.multiconnect.debug.DebugUtils;
import net.earthcomputer.multiconnect.protocols.generic.MulticonnectAddedRegistryEntries;
import net.minecraft.server.Bootstrap;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(Bootstrap.class)
public class BootstrapMixin {
    @Inject(method = "bootStrap", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;freezeBuiltins()V"))
    private static void preFreezeRegistries(CallbackInfo ci) {
        MulticonnectAddedRegistryEntries.register();
    }

    @Inject(method = "bootStrap", at = @At("TAIL"))
    private static void postInitialize(CallbackInfo ci) {
        if (DebugUtils.DUMP_REGISTRIES) {
            try {
                DebugUtils.dumpRegistries();
            } catch (IOException e) {
                LogManager.getLogger().error("Failed to dump registries", e);
            }
        }
    }
}
