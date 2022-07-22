package net.earthcomputer.multiconnect.protocols.v1_19.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.MulticonnectConfig;
import net.minecraft.client.GuiMessageTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiMessageTag.class)
public class GuiMessageTagMixin {
    @Inject(method = "chatNotSecure", at = @At("RETURN"), cancellable = true)
    private static void removeNotSecureIcon(CallbackInfoReturnable<GuiMessageTag> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_19 && MulticonnectConfig.INSTANCE.allowOldUnsignedChat == Boolean.TRUE) {
            GuiMessageTag returnValue = ci.getReturnValue();
            ci.setReturnValue(new GuiMessageTag(returnValue.indicatorColor(), null, returnValue.text(), returnValue.logTag()));
        }
    }
}
