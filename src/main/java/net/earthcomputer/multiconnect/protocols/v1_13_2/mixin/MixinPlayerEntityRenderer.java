package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class MixinPlayerEntityRenderer {

    @Unique private ThreadLocal<AbstractClientPlayerEntity> capturedPlayer = new ThreadLocal<>();

    @Inject(method = "method_4215", at = @At("HEAD"))
    private void capturePlayer(AbstractClientPlayerEntity player, double x, double y, double z, float yaw, float delta, CallbackInfo ci) {
        capturedPlayer.set(player);
    }

    @ModifyVariable(method = "method_4215", ordinal = 0, at = @At("HEAD"))
    private double modifyX(double x) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            Direction sleepingDir = capturedPlayer.get().getSleepingDirection();
            if (sleepingDir != null) {
                x -= sleepingDir.getOffsetX() * 0.4;
            }
        }
        return x;
    }

    @ModifyVariable(method = "method_4215", ordinal = 2, at = @At("HEAD"))
    private double modifyZ(double z) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            Direction sleepingDir = capturedPlayer.get().getSleepingDirection();
            if (sleepingDir != null) {
                z -= sleepingDir.getOffsetZ() * 0.4;
            }
        }
        capturedPlayer.set(null);
        return z;
    }

}
