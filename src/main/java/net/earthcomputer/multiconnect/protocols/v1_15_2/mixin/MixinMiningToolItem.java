package net.earthcomputer.multiconnect.protocols.v1_15_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.block.BlockState;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(MiningToolItem.class)
public class MixinMiningToolItem {

    @Inject(method="getMiningSpeedMultiplier", at = @At("RETURN"), cancellable = true)
    public void changeHoeEffectiveBlocks(ItemStack stack, BlockState state, CallbackInfoReturnable<Float> cir) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_15_2 && (Object)this instanceof HoeItem) {
            cir.setReturnValue(1.0F);
        }
    }
}
