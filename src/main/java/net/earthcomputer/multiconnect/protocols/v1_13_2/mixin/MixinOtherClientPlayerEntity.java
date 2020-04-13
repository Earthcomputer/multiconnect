package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import com.mojang.authlib.GameProfile;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RemoteClientPlayerEntity.class)
public abstract class MixinOtherClientPlayerEntity extends AbstractClientPlayerEntity {

    public MixinOtherClientPlayerEntity(ClientWorld world, GameProfile gameProfile) {
        super(world, gameProfile);
    }

    @Inject(method = "updatePose", at = @At("HEAD"))
    private void onUpdateSize(CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            Pose pose;
            if (isElytraFlying()) {
                pose = Pose.FALL_FLYING;
            } else if (isSleeping()) {
                pose = Pose.SLEEPING;
            } else if (isSwimming()) {
                pose = Pose.SWIMMING;
            } else if (isSpinAttacking()) {
                pose = Pose.SPIN_ATTACK;
            } else if (isCrouching() && !abilities.isFlying) {
                pose = Pose.CROUCHING;
            } else {
                pose = Pose.STANDING;
            }
            setPose(pose);
        }
    }

}
