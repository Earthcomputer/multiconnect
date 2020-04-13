package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntity {

    private static final EntitySize SNEAKING_DIMENSIONS_1_13_2 = EntitySize.flexible(0.6f, 1.65f);

    @Inject(method = "getSize", at = @At("HEAD"), cancellable = true)
    private void onGetDimensions(Pose pose, CallbackInfoReturnable<EntitySize> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            if (pose == Pose.CROUCHING) {
                ci.setReturnValue(SNEAKING_DIMENSIONS_1_13_2);
            }
        }
    }

    @ModifyConstant(method = "getStandingEyeHeight", constant = @Constant(floatValue = 1.27f))
    private float modifySneakEyeHeight(float prevEyeHeight) {
        if (ConnectionInfo.protocolVersion > Protocols.V1_13_2)
            return prevEyeHeight;
        else
            return 1.54f;
    }

}
