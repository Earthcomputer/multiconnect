package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.gui.screen.SplashScreen;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashScreen.class)
public class MixinSplashScreen {

    @Inject(method = "render", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/gui/screen/SplashScreen;applyCompleteTime:J"))
    private void onResourceReloadComplete(int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ConnectionInfo.protocol.setup(true);
        ConnectionInfo.stopReloadingResources();
    }

}
