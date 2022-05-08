package net.earthcomputer.multiconnect.mixin.debug;

import net.earthcomputer.multiconnect.impl.DebugUtils;
import net.minecraft.Bootstrap;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(Bootstrap.class)
public class MixinBootstrap {
    @Inject(method = "initialize", at = @At("TAIL"))
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
