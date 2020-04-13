package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Chunk.class)
public class MixinWorldChunk {

    @Unique private boolean shouldReplaceBlockEntity;

    @ModifyVariable(method = "setBlockState", ordinal = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;", ordinal = 0))
    private BlockState calcShouldReplaceBlockEntity(BlockState oldState, BlockPos pos, BlockState newState, boolean bl) {
        shouldReplaceBlockEntity = ConnectionInfo.protocol.shouldBlockChangeReplaceBlockEntity(oldState.getBlock(), newState.getBlock());
        return oldState;
    }

    @Redirect(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;removeTileEntity(Lnet/minecraft/util/math/BlockPos;)V"))
    private void redirectRemoveBlockEntity(World world, BlockPos pos) {
        if (shouldReplaceBlockEntity)
            world.removeTileEntity(pos);
    }

}
