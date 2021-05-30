package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntity {

    private static final EntityDimensions SNEAKING_DIMENSIONS_1_13_2 = EntityDimensions.changing(0.6f, 1.65f);

    @Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
    private void onGetDimensions(EntityPose pose, CallbackInfoReturnable<EntityDimensions> ci) {
        if (pose == EntityPose.CROUCHING) {
            if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
                ci.setReturnValue(PlayerEntity.STANDING_DIMENSIONS);
            } else if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
                ci.setReturnValue(SNEAKING_DIMENSIONS_1_13_2);
            }
        }
    }

    @ModifyConstant(method = "getActiveEyeHeight", constant = @Constant(floatValue = 1.27f))
    private float modifySneakEyeHeight(float prevEyeHeight) {
        if (ConnectionInfo.protocolVersion > Protocols.V1_13_2)
            return prevEyeHeight;
        else
            return 1.54f;
    }

}
