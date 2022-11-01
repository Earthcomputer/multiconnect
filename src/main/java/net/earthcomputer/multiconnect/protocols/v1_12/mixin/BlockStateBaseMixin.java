package net.earthcomputer.multiconnect.protocols.v1_12.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
    @Shadow public abstract Block getBlock();

    @Inject(method = "getPistonPushReaction", at = @At("HEAD"), cancellable = true)
    private void onHasBlockEntity(CallbackInfoReturnable<PushReaction> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2 && (getBlock() == Blocks.NOTE_BLOCK || getBlock() instanceof FlowerPotBlock)) {
            ci.setReturnValue(PushReaction.BLOCK);
        }
    }
}
