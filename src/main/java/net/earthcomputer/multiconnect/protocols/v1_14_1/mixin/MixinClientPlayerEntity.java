package net.earthcomputer.multiconnect.protocols.v1_14_1.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {

    @Redirect(method = "tickMovement",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isWalking()Z")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSwimming()Z", ordinal = 0))
    public boolean redirectIsSneakingWhileSwimming(ClientPlayerEntity _this) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_14_1)
            return false;
        else
            return _this.isSwimming();
    }

    @Inject(method = "isWalking", at = @At("HEAD"), cancellable = true)
    public void easierUnderwaterSprinting(CallbackInfoReturnable<Boolean> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_14_1) {
            ci.setReturnValue(((ClientPlayerEntity) (Object) this).input.movementForward >= 0.8);
        }
    }

    @Redirect(
        method = "tickMovement()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/input/Input;hasForwardMovement()Z",
            ordinal = 0
        )
    )
    private boolean disableSprintSneak(Input input) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_14_1) {
            return input.movementForward >= 0.8F;
        }
        return input.hasForwardMovement();
    }

}
