package net.earthcomputer.multiconnect.protocols.v1_13.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {

    @Shadow @Final private Abilities abilities;
    private static final EntityDimensions MULTICONNECT_SNEAKING_DIMENSIONS_1_13_2 = EntityDimensions.scalable(0.6f, 1.65f);

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "updatePlayerPose", at = @At("HEAD"), cancellable = true)
    private void onUpdatePose(CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            Pose pose;
            if (isFallFlying()) {
                pose = Pose.FALL_FLYING;
            } else if (isSleeping()) {
                pose = Pose.SLEEPING;
            } else if (isSwimming()) {
                pose = Pose.SWIMMING;
            } else if (isAutoSpinAttack()) {
                pose = Pose.SPIN_ATTACK;
            } else if (isShiftKeyDown() && !abilities.flying) {
                pose = Pose.CROUCHING;
            } else {
                pose = Pose.STANDING;
            }
            setPose(pose);
            ci.cancel();
        }
    }

    @Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
    private void onGetDimensions(Pose pose, CallbackInfoReturnable<EntityDimensions> ci) {
        if (pose == Pose.CROUCHING) {
            if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
                ci.setReturnValue(Player.STANDING_DIMENSIONS);
            } else if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
                ci.setReturnValue(MULTICONNECT_SNEAKING_DIMENSIONS_1_13_2);
            }
        }
    }

    @ModifyConstant(method = "getStandingEyeHeight", constant = @Constant(floatValue = 1.27f))
    private float modifySneakEyeHeight(float prevEyeHeight) {
        if (ConnectionInfo.protocolVersion > Protocols.V1_13_2)
            return prevEyeHeight;
        else
            return 1.54f;
    }

}
