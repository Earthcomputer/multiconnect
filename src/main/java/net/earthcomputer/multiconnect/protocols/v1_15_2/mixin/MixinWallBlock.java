package net.earthcomputer.multiconnect.protocols.v1_15_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallBlock;
import net.minecraft.block.enums.WallShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WallBlock.class)
public class MixinWallBlock {
    @Inject(method = "getPlacementState", at = @At("RETURN"), cancellable = true)
    private void onGetPlacementState(CallbackInfoReturnable<BlockState> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_15_2) {
            ci.setReturnValue(oldWallPlacementLogic(ci.getReturnValue()));
        }
    }

    @Inject(method = "getStateForNeighborUpdate", at = @At("RETURN"), cancellable = true)
    private void onGetStateForNeighborUpdate(CallbackInfoReturnable<BlockState> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_15_2) {
            ci.setReturnValue(oldWallPlacementLogic(ci.getReturnValue()));
        }
    }

    @Unique
    private static BlockState oldWallPlacementLogic(BlockState state) {
        boolean addUp = false;
        if (state.get(WallBlock.NORTH_SHAPE) == WallShape.TALL) {
            state = state.with(WallBlock.NORTH_SHAPE, WallShape.LOW);
            addUp = true;
        }
        if (state.get(WallBlock.EAST_SHAPE) == WallShape.TALL) {
            state = state.with(WallBlock.EAST_SHAPE, WallShape.LOW);
            addUp = true;
        }
        if (state.get(WallBlock.SOUTH_SHAPE) == WallShape.TALL) {
            state = state.with(WallBlock.SOUTH_SHAPE, WallShape.LOW);
            addUp = true;
        }
        if (state.get(WallBlock.WEST_SHAPE) == WallShape.TALL) {
            state = state.with(WallBlock.WEST_SHAPE, WallShape.LOW);
            addUp = true;
        }
        if (addUp) {
            state = state.with(WallBlock.UP, true);
        }
        return state;
    }
}
