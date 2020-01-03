package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class MixinBlock {

    @Inject(method = "updateNeighborStates", at = @At("TAIL"))
    private void onUpdateNeighborStates(BlockState state, IWorld world, BlockPos pos, int flags, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            BlockState newState = state;
            try (BlockPos.PooledMutable otherPos = BlockPos.PooledMutable.get()) {
                for (Direction side : Direction.values()) {
                    otherPos.set(pos).setOffset(side);
                    newState = newState.getStateForNeighborUpdate(side, world.getBlockState(otherPos), world, pos, otherPos);
                }
            }
            Block.replaceBlock(state, newState, world, pos, flags);
        }
    }

}
