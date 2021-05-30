package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LadderBlock.class)
public class MixinLadderBlock {
    @Unique private static final VoxelShape EAST_SHAPE_1_8 = Block.createCuboidShape(0, 0, 0, 2, 16, 16);
    @Unique private static final VoxelShape WEST_SHAPE_1_8 = Block.createCuboidShape(14, 0, 0, 16, 16, 16);
    @Unique private static final VoxelShape SOUTH_SHAPE_1_8 = Block.createCuboidShape(0, 0, 0, 16, 16, 2);
    @Unique private static final VoxelShape NORTH_SHAPE_1_8 = Block.createCuboidShape(0, 0, 14, 16, 16, 16);
    
    @Inject(method = "getOutlineShape", at = @At("HEAD"), cancellable = true)
    private void onGetOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            switch (state.get(LadderBlock.FACING)) {
                case NORTH -> ci.setReturnValue(NORTH_SHAPE_1_8);
                case SOUTH -> ci.setReturnValue(SOUTH_SHAPE_1_8);
                case WEST -> ci.setReturnValue(WEST_SHAPE_1_8);
                default -> ci.setReturnValue(EAST_SHAPE_1_8);
            }
        }
    }
}
