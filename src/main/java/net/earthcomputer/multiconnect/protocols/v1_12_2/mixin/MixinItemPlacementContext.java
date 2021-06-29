package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemPlacementContext.class)
public class MixinItemPlacementContext {
    @Inject(method = "getPlayerLookDirection", at = @At("HEAD"), cancellable = true)
    private void get112LookDirection(CallbackInfoReturnable<Direction> ci) {
        ItemPlacementContext self = (ItemPlacementContext) (Object) this;

        PlayerEntity player = self.getPlayer();
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2 && player != null) {
            BlockPos placementPos = self.getBlockPos();
            if (Math.abs(player.getX() - (placementPos.getX() + 0.5)) < 2 && Math.abs(player.getZ() - (placementPos.getZ() + 0.5)) < 2) {
                double eyeY = player.getY() + player.getEyeHeight(player.getPose());

                if (eyeY - placementPos.getY() > 2) {
                    ci.setReturnValue(Direction.DOWN);
                    return;
                }

                if (placementPos.getY() - eyeY > 0) {
                    ci.setReturnValue(Direction.UP);
                    return;
                }
            }

            ci.setReturnValue(player.getHorizontalFacing());
        }
    }
}
