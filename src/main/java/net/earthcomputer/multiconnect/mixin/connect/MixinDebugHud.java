package net.earthcomputer.multiconnect.mixin.connect;

import net.earthcomputer.multiconnect.connect.ConnectionMode;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class)
public class MixinDebugHud {
    @Inject(method = "getLeftText", at = @At("RETURN"))
    private void addServerVersion(CallbackInfoReturnable<List<String>> ci) {
        ci.getReturnValue().add("[multiconnect] Server version: " + ConnectionMode.byValue(ConnectionInfo.protocolVersion).getName() + " (" + ConnectionInfo.protocolVersion + ")");
    }
}
