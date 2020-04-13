package net.earthcomputer.multiconnect.protocols.v1_14_1.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {

    @Redirect(method = "isCrouching", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/player/ClientPlayerEntity;isSwimming()Z"))
    public boolean redirectIsSneakingWhileSwimming(ClientPlayerEntity _this) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_14_1)
            return false;
        else
            return _this.isSwimming();
    }

    @Inject(method = "func_223110_ee", at = @At("HEAD"), cancellable = true)
    public void easierUnderwaterSprinting(CallbackInfoReturnable<Boolean> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_14_1) {
            ci.setReturnValue(((ClientPlayerEntity) (Object) this).movementInput.moveForward >= 0.8);
        }
    }

}
