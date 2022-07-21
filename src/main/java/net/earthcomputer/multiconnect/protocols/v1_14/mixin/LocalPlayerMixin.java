package net.earthcomputer.multiconnect.protocols.v1_14.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Shadow
    public Input input;

    @Redirect(method = "aiStep",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasEnoughImpulseToStartSprinting()Z")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSwimming()Z", ordinal = 0))
    public boolean redirectIsSneakingWhileSwimming(LocalPlayer self) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_14_1)
            return false;
        else
            return self.isSwimming();
    }

    @Inject(method = "hasEnoughImpulseToStartSprinting", at = @At("HEAD"), cancellable = true)
    public void easierUnderwaterSprinting(CallbackInfoReturnable<Boolean> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_14_1) {
            ci.setReturnValue(((LocalPlayer) (Object) this).input.forwardImpulse >= 0.8);
        }
    }

    @Redirect(
        method = "aiStep",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/Input;hasForwardImpulse()Z",
            ordinal = 0
        )
    )
    private boolean disableSprintSneak(Input input) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_14_1) {
            return input.forwardImpulse >= 0.8F;
        }
        return input.hasForwardImpulse();
    }

    @Inject(
        method = "aiStep",
        slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isControlledCamera()Z")),
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/Input;shiftKeyDown:Z", ordinal = 0)
    )
    private void undoSneakSlowdownForFly(CallbackInfo ci) {
        // Versions older than 1.15 undo sneaking effects for
        // flying by just multiplying it back by 3 while versions
        // newer than 1.13.2 have the condition that the player
        // isn't flying included in the inSneakingPose condition.
        // Versions 1.14 to 1.14.4 use a broken mix of that.
        if (ConnectionInfo.protocolVersion <= Protocols.V1_14_4) {
            if (this.input.shiftKeyDown) {
                // Copied from 1.14.4
                // Undo sneaking slowdown
                this.input.leftImpulse = (float)((double)this.input.leftImpulse / 0.3D);
                this.input.forwardImpulse = (float)((double)this.input.forwardImpulse / 0.3D);
            }
        }
    }
}
