package net.earthcomputer.multiconnect.protocols.v1_10.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlowerPotBlock.class)
public class FlowerPotBlockMixin {

    @Shadow @Final private Block content;

    @Inject(method = "use", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/FlowerPotBlock;content:Lnet/minecraft/world/level/block/Block;", ordinal = 0), cancellable = true)
    private void cancelEmptyingFlowerPot(CallbackInfoReturnable<InteractionResult> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_10 && content != Blocks.AIR) {
            ci.setReturnValue(InteractionResult.CONSUME);
        }
    }

}
