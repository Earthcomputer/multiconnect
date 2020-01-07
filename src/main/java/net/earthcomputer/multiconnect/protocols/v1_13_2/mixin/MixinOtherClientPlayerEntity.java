package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import com.mojang.authlib.GameProfile;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityPose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OtherClientPlayerEntity.class)
public abstract class MixinOtherClientPlayerEntity extends AbstractClientPlayerEntity {

    public MixinOtherClientPlayerEntity(ClientWorld world, GameProfile gameProfile) {
        super(world, gameProfile);
    }

    @Inject(method = "updateSize", at = @At("HEAD"))
    private void onUpdateSize(CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            EntityPose pose;
            if (isFallFlying()) {
                pose = EntityPose.FALL_FLYING;
            } else if (isSleeping()) {
                pose = EntityPose.SLEEPING;
            } else if (isSwimming()) {
                pose = EntityPose.SWIMMING;
            } else if (isUsingRiptide()) {
                pose = EntityPose.SPIN_ATTACK;
            } else if (isSneaking() && !abilities.flying) {
                pose = EntityPose.CROUCHING;
            } else {
                pose = EntityPose.STANDING;
            }
            setPose(pose);
        }
    }

}
