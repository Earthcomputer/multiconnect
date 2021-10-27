package net.earthcomputer.multiconnect.protocols.v1_14_4.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity {
    @Shadow public Input input;

    @Inject(
        method = "tickMovement()V",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/input/Input;sneaking:Z",
            ordinal = 3
        )
    )
    private void undoSneakSlowdownForFly(CallbackInfo ci) {
        // Versions older than 1.15 undo sneaking effects for
        // flying by just multiplying it back by 3 while versions
        // newer than 1.13.2 have the condition that the player
        // isn't flying included in the inSneakingPose condition.
        // Versions 1.14 to 1.14.4 use a broken mix of that.
        if (ConnectionInfo.protocolVersion <= Protocols.V1_14_4) {
            if (this.input.sneaking) {
                // Copied from 1.14.4
                // Undo sneaking slowdown
                this.input.movementSideways = (float)((double)this.input.movementSideways / 0.3D);
                this.input.movementForward = (float)((double)this.input.movementForward / 0.3D);
            }
        }
    }
}
