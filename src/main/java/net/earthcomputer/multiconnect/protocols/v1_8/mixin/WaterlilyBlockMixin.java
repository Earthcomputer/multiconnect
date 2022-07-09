package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WaterlilyBlock.class)
public class WaterlilyBlockMixin {

    private static final VoxelShape SHAPE_1_8 = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 0.015625D /* 1 / 64 */ * 16, 16.0D);

    @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
    public void changeBoundingBox(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if(ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            cir.setReturnValue(SHAPE_1_8);
        }
    }

}
