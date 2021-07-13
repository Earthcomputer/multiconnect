package net.earthcomputer.multiconnect.integrationtest.mixin;

import net.earthcomputer.multiconnect.integrationtest.IntegrationTest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.wagyourtail.jsmacros.core.Core;

import java.io.IOException;
import java.net.URISyntaxException;

@Mixin(value = Core.class, remap = false)
public class MixinJsMacrosCore {
    @Inject(method = "createInstance", at = @At("HEAD"))
    private static void preCreateCore(CallbackInfoReturnable<Core> ci) {
        try {
            IntegrationTest.syncMacrosFolder();
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
