package net.earthcomputer.multiconnect.protocols.v1_13.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {

    @Inject(method = "getRenderOffset(Lnet/minecraft/client/player/AbstractClientPlayer;F)Lnet/minecraft/world/phys/Vec3;", at = @At("RETURN"), cancellable = true)
    private void injectSleepingOffset(AbstractClientPlayer player, float delta, CallbackInfoReturnable<Vec3> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            Direction sleepingDir = player.getBedOrientation();
            if (sleepingDir != null) {
                ci.setReturnValue(ci.getReturnValue().subtract(sleepingDir.getStepX() * 0.4, 0, sleepingDir.getStepZ() * 0.4));
            }
        }
    }

}
