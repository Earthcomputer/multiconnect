package net.earthcomputer.multiconnect.protocols.v1_12.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Redirect(method = "travel",
            slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/world/effect/MobEffects;LEVITATION:Lnet/minecraft/world/effect/MobEffect;", ordinal = 0)),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;resetFallDistance()V", ordinal = 0))
    private void dontResetLevitationFallDistance(LivingEntity instance) {
        if (ConnectionInfo.protocolVersion > Protocols.V1_12_2) {
            instance.resetFallDistance();
        }
    }

    // TODO: when @At(reverse = true) is added to mixin:
    //@Redirect(method = "travel", slice = @Slice(to = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getBaseMovementSpeedMultiplier()F")), at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSprinting()Z", ordinal = 0, reverse = true))
    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSprinting()Z", ordinal = 0))
    private boolean modifySwimSprintSpeed(LivingEntity self) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            return false;
        }
        return self.isSprinting();
    }

    @Inject(method = "getFluidFallingAdjustedMovement", at = @At("HEAD"), cancellable = true)
    private void modifySwimSprintFallSpeed(double gravity, boolean movingDown, Vec3 velocity, CallbackInfoReturnable<Vec3> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2 && !isNoGravity()) {
            ci.setReturnValue(new Vec3(velocity.x, velocity.y - 0.02, velocity.z));
        }
    }

    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getFluidHeight(Lnet/minecraft/tags/TagKey;)D"))
    private double redirectFluidHeight(LivingEntity entity, TagKey<Fluid> tag) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2 && tag == FluidTags.WATER) {
            // If you're in water, you're in water, even if you're almost at the surface
            if (entity.getFluidHeight(tag) > 0)
                return 1;
        }
        return entity.getFluidHeight(tag);
    }

}
