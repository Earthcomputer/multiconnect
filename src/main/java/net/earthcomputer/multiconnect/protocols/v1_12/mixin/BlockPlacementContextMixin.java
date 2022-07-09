package net.earthcomputer.multiconnect.protocols.v1_12.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockPlaceContext.class)
public class BlockPlacementContextMixin {
    @Inject(method = "getNearestLookingDirection", at = @At("HEAD"), cancellable = true)
    private void get112LookDirection(CallbackInfoReturnable<Direction> ci) {
        BlockPlaceContext self = (BlockPlaceContext) (Object) this;

        Player player = self.getPlayer();
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2 && player != null) {
            BlockPos placementPos = self.getClickedPos();
            // don't center the BlockPos on 1.10 and below
            double blockPosCenterFactor = ConnectionInfo.protocolVersion > Protocols.V1_10 ? 0.5 : 0;

            if (Math.abs(player.getX() - (placementPos.getX() + blockPosCenterFactor)) < 2 && Math.abs(player.getZ() - (placementPos.getZ() + blockPosCenterFactor)) < 2) {
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

            ci.setReturnValue(player.getDirection());
        }
    }
}
