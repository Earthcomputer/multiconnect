package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {

    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Redirect(method = "travel",
            slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/entity/effect/StatusEffects;LEVITATION:Lnet/minecraft/entity/effect/StatusEffect;", ordinal = 0)),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;onLanding()V", ordinal = 0))
    private void dontResetLevitationFallDistance(LivingEntity instance) {
        if (ConnectionInfo.protocolVersion > Protocols.V1_12_2) {
            instance.onLanding(); // onLanding is badly named, should be resetFallDistance
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

    @Inject(method = "method_26317(DZLnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;", at = @At("HEAD"), cancellable = true)
    private void modifySwimSprintFallSpeed(double gravity, boolean movingDown, Vec3d velocity, CallbackInfoReturnable<Vec3d> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2 && !hasNoGravity()) {
            ci.setReturnValue(new Vec3d(velocity.x, velocity.y - 0.02, velocity.z));
        }
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getFluidHeight(Lnet/minecraft/tag/Tag;)D"))
    private double redirectFluidHeight(LivingEntity entity, Tag<Fluid> tag) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2 && tag == FluidTags.WATER) {
            // If you're in water, you're in water, even if you're almost at the surface
            if (entity.getFluidHeight(tag) > 0)
                return 1;
        }
        return entity.getFluidHeight(tag);
    }

}
