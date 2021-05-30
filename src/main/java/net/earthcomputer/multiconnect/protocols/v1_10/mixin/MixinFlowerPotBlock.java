package net.earthcomputer.multiconnect.protocols.v1_10.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlowerPotBlock.class)
public class MixinFlowerPotBlock {

    @Shadow @Final private Block content;

    @Inject(method = "onUse", at = @At(value = "FIELD", target = "Lnet/minecraft/block/FlowerPotBlock;content:Lnet/minecraft/block/Block;", ordinal = 0), cancellable = true)
    private void cancelEmptyingFlowerPot(CallbackInfoReturnable<ActionResult> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_10 && content != Blocks.AIR) {
            ci.setReturnValue(ActionResult.CONSUME);
        }
    }

}
