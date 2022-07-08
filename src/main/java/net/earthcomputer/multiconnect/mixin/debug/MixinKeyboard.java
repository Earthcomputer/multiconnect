package net.earthcomputer.multiconnect.mixin.debug;

import net.earthcomputer.multiconnect.debug.DebugUtils;
import net.earthcomputer.multiconnect.debug.PacketReplayMenuScreen;
import net.earthcomputer.multiconnect.impl.MulticonnectConfig;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class MixinKeyboard {
    private static boolean isEnabled(){
        return (Boolean.getBoolean("multiconnect.debugKey") || Boolean.TRUE.equals(MulticonnectConfig.INSTANCE.debugKey));
    }

    @Inject(method = "onKey",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/InputUtil;fromKeyCode(II)Lnet/minecraft/client/util/InputUtil$Key;"),
            allow = 1)
    private void onKeyInGame(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (isEnabled() && key == GLFW.GLFW_KEY_F8 && action == GLFW.GLFW_PRESS) {
            DebugUtils.onDebugKey();
        }
    }

    @Inject(method = "onKey",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;wrapScreenError(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V"),
            allow = 1)
    private void onKeyInScreen(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (isEnabled() && key == GLFW.GLFW_KEY_F8 && action == GLFW.GLFW_PRESS) {
            Screen currentScreen = MinecraftClient.getInstance().currentScreen;
            if (currentScreen instanceof TitleScreen) {
                MinecraftClient.getInstance().setScreen(new PacketReplayMenuScreen());
            }
        }
    }
}
