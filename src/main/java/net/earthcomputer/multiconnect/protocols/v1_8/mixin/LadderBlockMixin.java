package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LadderBlock.class)
public class LadderBlockMixin {
    @Unique private static final VoxelShape EAST_SHAPE_1_8 = Block.box(0, 0, 0, 2, 16, 16);
    @Unique private static final VoxelShape WEST_SHAPE_1_8 = Block.box(14, 0, 0, 16, 16, 16);
    @Unique private static final VoxelShape SOUTH_SHAPE_1_8 = Block.box(0, 0, 0, 16, 16, 2);
    @Unique private static final VoxelShape NORTH_SHAPE_1_8 = Block.box(0, 0, 14, 16, 16, 16);
    
    @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
    private void onGetShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            switch (state.getValue(LadderBlock.FACING)) {
                case NORTH -> ci.setReturnValue(NORTH_SHAPE_1_8);
                case SOUTH -> ci.setReturnValue(SOUTH_SHAPE_1_8);
                case WEST -> ci.setReturnValue(WEST_SHAPE_1_8);
                default -> ci.setReturnValue(EAST_SHAPE_1_8);
            }
        }
    }
}
