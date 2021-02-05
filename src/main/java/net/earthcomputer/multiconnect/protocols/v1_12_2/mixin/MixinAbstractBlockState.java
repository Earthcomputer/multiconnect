package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerPotBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class MixinAbstractBlockState {
    @Shadow public abstract Block getBlock();

    @Inject(method = "hasBlockEntity", at = @At("HEAD"), cancellable = true)
    private void onHasBlockEntity(CallbackInfoReturnable<Boolean> ci) {
        if (getBlock() == Blocks.NOTE_BLOCK || getBlock() instanceof FlowerPotBlock) {
            ci.setReturnValue(ConnectionInfo.protocolVersion <= Protocols.V1_12_2);
        }
    }
}
