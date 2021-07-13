package net.earthcomputer.multiconnect.integrationtest.mixin;

import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InputUtil.class)
public class MixinInputUtil {
    @Inject(method = "isKeyPressed", at = @At("HEAD"), cancellable = true)
    private static void onIsKeyPressed(CallbackInfoReturnable<Boolean> ci) {
        ci.setReturnValue(false);
    }

    @Inject(method = {"setKeyboardCallbacks", "setMouseCallbacks", "setCursorParameters"}, at = @At("HEAD"), cancellable = true)
    private static void onSetCallbacks(CallbackInfo ci) {
        ci.cancel();
    }
}
