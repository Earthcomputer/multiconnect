package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public class MixinPlayerEntityRenderer {

    @Inject(method = "getRenderOffset", at = @At("RETURN"), cancellable = true)
    private void injectSleepingOffset(AbstractClientPlayerEntity player, float delta, CallbackInfoReturnable<Vec3d> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            Direction sleepingDir = player.getBedDirection();
            if (sleepingDir != null) {
                ci.setReturnValue(ci.getReturnValue().subtract(sleepingDir.getXOffset() * 0.4, 0, sleepingDir.getZOffset() * 0.4));
            }
        }
    }

}
