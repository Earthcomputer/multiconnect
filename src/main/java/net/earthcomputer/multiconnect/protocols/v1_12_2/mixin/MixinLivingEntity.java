package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {

    @Unique private float oldFallDistance;

    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "travel",
            slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/potion/Effects;LEVITATION:Lnet/minecraft/potion/Effect;", ordinal = 0)),
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;fallDistance:F", ordinal = 0))
    private void captureFallDistance(Vec3d travelVec, CallbackInfo ci) {
        oldFallDistance = fallDistance;
    }

    @Inject(method = "travel",
            slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/potion/Effects;LEVITATION:Lnet/minecraft/potion/Effect;", ordinal = 0)),
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;fallDistance:F", ordinal = 0, shift = At.Shift.AFTER))
    private void dontResetLevitationFallDistance(Vec3d travelVec, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            fallDistance = oldFallDistance;
        }
    }

    // TODO: when @At(reverse = true) is added to mixin:
    //@Redirect(method = "travel", slice = @Slice(to = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getBaseMovementSpeedMultiplier()F")), at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSprinting()Z", ordinal = 0, reverse = true))
    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSprinting()Z", ordinal = 0))
    private boolean modifySwimSprintSpeed(LivingEntity self) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            return false;
        }
        return self.isSprinting();
    }

    // TODO: when @At(reverse = true) is added to mixin:
    //@Redirect(method = "travel", slice = @Slice(to = @At(value = "INVOKE", target = "Ljava/lang/Math;abs(D)D", ordinal = 0, remap = false)), at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSprinting()Z", ordinal = 0, reverse = true))
    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSprinting()Z", ordinal = 1))
    private boolean modifySwimSprintFallSpeed(LivingEntity self) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            Vec3d velocity = getMotion();
            setVelocity(velocity.x, velocity.y - 0.02, velocity.z);
            return true; // to skip the if statement body
        }
        return self.isSprinting();
    }

    @Redirect(method = "livingTick", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;submergedHeight:D"))
    private double redirectWaterHeight(LivingEntity entity) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            // If you're in water, you're in water, even if you're almost at the surface
            if (entity.getSubmergedHeight() > 0)
                return 1;
        }
        return entity.getSubmergedHeight();
    }

}
