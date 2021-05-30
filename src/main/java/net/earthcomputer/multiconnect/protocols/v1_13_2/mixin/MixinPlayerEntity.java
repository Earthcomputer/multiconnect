package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity {

    @Shadow @Final private PlayerAbilities abilities;
    private static final EntityDimensions SNEAKING_DIMENSIONS_1_13_2 = EntityDimensions.changing(0.6f, 1.65f);

    protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "updatePose", at = @At("HEAD"), cancellable = true)
    private void onUpdatePose(CallbackInfo ci) {
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
            ci.cancel();
        }
    }

    @Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
    private void onGetDimensions(EntityPose pose, CallbackInfoReturnable<EntityDimensions> ci) {
        if (pose == EntityPose.CROUCHING) {
            if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
                ci.setReturnValue(PlayerEntity.STANDING_DIMENSIONS);
            } else if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
                ci.setReturnValue(SNEAKING_DIMENSIONS_1_13_2);
            }
        }
    }

    @ModifyConstant(method = "getActiveEyeHeight", constant = @Constant(floatValue = 1.27f))
    private float modifySneakEyeHeight(float prevEyeHeight) {
        if (ConnectionInfo.protocolVersion > Protocols.V1_13_2)
            return prevEyeHeight;
        else
            return 1.54f;
    }

}
