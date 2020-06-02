package net.earthcomputer.multiconnect.protocols.v1_10.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.block.FenceBlock;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FenceBlock.class)
public class MixinFenceBlock {

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void onOnUse(CallbackInfoReturnable<ActionResult> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_10) {
            ci.setReturnValue(ActionResult.SUCCESS);
        }
    }

}
