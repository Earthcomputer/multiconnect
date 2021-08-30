package net.earthcomputer.multiconnect.mixin.debug;

import net.earthcomputer.multiconnect.impl.DebugUtils;
import net.minecraft.client.Keyboard;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class MixinKeyboard {
    @Unique
    private static final boolean MULTICONNECT_DEBUG_KEY = Boolean.getBoolean("multiconnect.debugKey");

    @Inject(method = "onKey",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/InputUtil;fromKeyCode(II)Lnet/minecraft/client/util/InputUtil$Key;"),
            allow = 1)
    private void onOnKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (MULTICONNECT_DEBUG_KEY && key == GLFW.GLFW_KEY_F8 && action == GLFW.GLFW_PRESS) {
            DebugUtils.onDebugKey();
        }
    }
}
