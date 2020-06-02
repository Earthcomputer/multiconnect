package net.earthcomputer.multiconnect.protocols.v1_10.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.block.CauldronBlock;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CauldronBlock.class)
public class MixinCauldronBlock {

    @Inject(method = "onUse",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/potion/PotionUtil;getPotion(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/potion/Potion;")),
            at = @At(value = "RETURN", ordinal = 0),
            cancellable = true)
    private void cancelWaterBottleUse(CallbackInfoReturnable<ActionResult> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_10) {
            ci.setReturnValue(ActionResult.PASS);
        }
    }

}
