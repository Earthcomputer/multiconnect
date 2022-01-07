package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {
    @ModifyConstant(method = "tickMovement", constant = @Constant(doubleValue = 0.003D))
    public double modifyVelocityZero(final double constant) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) return 0.005D;
        return constant;
    }

    @Inject(method = "canEnterTrapdoor", at = @At("HEAD"), cancellable = true)
    private void onCanEnterTrapdoor(CallbackInfoReturnable<Boolean> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            ci.setReturnValue(false);
        }
    }

}
