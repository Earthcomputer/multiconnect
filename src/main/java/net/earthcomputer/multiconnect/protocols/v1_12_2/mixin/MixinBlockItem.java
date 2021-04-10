package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class MixinBlockItem {

    @Inject(method = "canPlace", at = @At("HEAD"), cancellable = true)
    private void onCanPlace(ItemPlacementContext context, BlockState state, CallbackInfoReturnable<Boolean> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            Block block = state.getBlock();
            if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST) {
                World world = context.getWorld();
                BlockPos pos = context.getBlockPos();
                boolean foundAdjChest = false;
                for (Direction dir : Direction.Type.HORIZONTAL) {
                    BlockState otherState = world.getBlockState(pos.offset(dir));
                    if (otherState.getBlock() == block) {
                        if (foundAdjChest) {
                            ci.setReturnValue(false);
                            return;
                        }
                        foundAdjChest = true;
                        if (otherState.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
                            ci.setReturnValue(false);
                            return;
                        }
                    }
                }
            }
        }
    }

}
