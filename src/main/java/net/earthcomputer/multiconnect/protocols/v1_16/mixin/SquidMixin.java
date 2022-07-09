package net.earthcomputer.multiconnect.protocols.v1_16.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.world.entity.animal.Squid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Squid.class)
public class SquidMixin {
    @Inject(method = "canBeLeashed", at = @At("HEAD"), cancellable = true)
    private void cancelLeashing(CallbackInfoReturnable<Boolean> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_16_5) {
            ci.setReturnValue(false);
        }
    }
}
