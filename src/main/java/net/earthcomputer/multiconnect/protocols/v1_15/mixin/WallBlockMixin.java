package net.earthcomputer.multiconnect.protocols.v1_15.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WallSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WallBlock.class)
public class WallBlockMixin {
    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void onGetStateForPlacement(CallbackInfoReturnable<BlockState> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_15_2) {
            ci.setReturnValue(multiconnect_oldWallPlacementLogic(ci.getReturnValue()));
        }
    }

    @Inject(method = "updateShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", at = @At("RETURN"), cancellable = true)
    private void onUpdateShape(CallbackInfoReturnable<BlockState> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_15_2) {
            ci.setReturnValue(multiconnect_oldWallPlacementLogic(ci.getReturnValue()));
        }
    }

    @Unique
    private static BlockState multiconnect_oldWallPlacementLogic(BlockState state) {
        boolean addUp = false;
        if (state.getValue(WallBlock.NORTH_WALL) == WallSide.TALL) {
            state = state.setValue(WallBlock.NORTH_WALL, WallSide.LOW);
            addUp = true;
        }
        if (state.getValue(WallBlock.EAST_WALL) == WallSide.TALL) {
            state = state.setValue(WallBlock.EAST_WALL, WallSide.LOW);
            addUp = true;
        }
        if (state.getValue(WallBlock.SOUTH_WALL) == WallSide.TALL) {
            state = state.setValue(WallBlock.SOUTH_WALL, WallSide.LOW);
            addUp = true;
        }
        if (state.getValue(WallBlock.WEST_WALL) == WallSide.TALL) {
            state = state.setValue(WallBlock.WEST_WALL, WallSide.LOW);
            addUp = true;
        }
        if (addUp) {
            state = state.setValue(WallBlock.UP, true);
        }
        return state;
    }
}
