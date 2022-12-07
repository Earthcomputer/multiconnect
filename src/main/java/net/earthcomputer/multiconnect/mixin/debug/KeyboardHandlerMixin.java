package net.earthcomputer.multiconnect.mixin.debug;

import net.earthcomputer.multiconnect.debug.DebugUtils;
import net.earthcomputer.multiconnect.impl.MulticonnectConfig;
import net.minecraft.client.KeyboardHandler;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    private static boolean isEnabled(){
        return (Boolean.getBoolean("multiconnect.debugKey") || Boolean.TRUE.equals(MulticonnectConfig.INSTANCE.debugKey));
    }

    @Inject(method = "keyPress",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/InputConstants;getKey(II)Lcom/mojang/blaze3d/platform/InputConstants$Key;"),
            allow = 1)
    private void onKeyInGame(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (isEnabled() && key == GLFW.GLFW_KEY_F8 && action == GLFW.GLFW_PRESS) {
            DebugUtils.onDebugKey();
        }
    }
}
