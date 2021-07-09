package net.earthcomputer.multiconnect.protocols.v1_16_5.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.entity.passive.SquidEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SquidEntity.class)
public class MixinSquidEntity {
    @Inject(method = "canBeLeashedBy", at = @At("HEAD"), cancellable = true)
    private void cancelLeashing(CallbackInfoReturnable<Boolean> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_16_5) {
            ci.setReturnValue(false);
        }
    }
}
