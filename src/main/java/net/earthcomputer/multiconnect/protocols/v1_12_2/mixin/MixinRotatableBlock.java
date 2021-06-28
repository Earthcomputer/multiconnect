package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.block.CommandBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.ObserverBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({PistonBlock.class, ObserverBlock.class, CommandBlock.class, DispenserBlock.class})
public class MixinRotatableBlock {
    @Redirect(method = "getPlacementState", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemPlacementContext;getPlayerLookDirection()Lnet/minecraft/util/math/Direction;"))
    private Direction get112LookDirection(ItemPlacementContext context) {
        PlayerEntity player = context.getPlayer();
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2 && player != null) {
            BlockPos placementPos = context.getBlockPos();
            if (Math.abs(player.getX() - (placementPos.getX() + 0.5)) < 2 && Math.abs(player.getZ() - (placementPos.getZ() + 0.5)) < 2) {
                double eyeY = player.getY() + player.getEyeHeight(player.getPose());

                if (eyeY - placementPos.getY() > 2) {
                    return Direction.UP;
                }

                if (placementPos.getY() - eyeY > 0) {
                    return Direction.DOWN;
                }
            }

            return player.getHorizontalFacing().getOpposite();
        } else {
            return context.getPlayerLookDirection();
        }
    }
}
