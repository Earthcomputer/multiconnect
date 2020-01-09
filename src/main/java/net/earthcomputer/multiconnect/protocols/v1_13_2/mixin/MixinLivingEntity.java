package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {

    @Shadow protected boolean jumping;

    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyConstant(method = "getMovementSpeed(F)F", constant = @Constant(floatValue = 0.21600002f))
    private float modifyMovementFactor(float oldFactor) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            return 0.546f * 0.546f * 0.546f;
        }
        return oldFactor;
    }

    @Redirect(method = "travel", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;jumping:Z"))
    private boolean disableJumpOnLadder(LivingEntity self) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            return false;
        }
        return jumping;
    }

    @Redirect(method = "travel",
            slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/entity/effect/StatusEffects;DOLPHINS_GRACE:Lnet/minecraft/entity/effect/StatusEffect;")),
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;horizontalCollision:Z", ordinal = 0))
    private boolean disableClimbing(LivingEntity self) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            return false;
        }
        return horizontalCollision;
    }

    @Unique private double travel_d;

    @Inject(method = "travel",
            slice = @Slice(from = @At(value = "INVOKE", target = "Ljava/lang/Math;abs(D)D", ordinal = 0, remap = false)),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setVelocity(DDD)V", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void capture_d(Vec3d travelVec, CallbackInfo ci, double d) {
        travel_d = d;
    }

    @ModifyArg(method = "travel",
            index = 1,
            slice = @Slice(from = @At(value = "INVOKE", target = "Ljava/lang/Math;abs(D)D", ordinal = 0, remap = false)),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setVelocity(DDD)V", ordinal = 0))
    private double modifyYVelocity(double yVelocity) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            double yVel = getVelocity().y;
            if (yVel <= 0 && Math.abs(yVel - 0.005) >= 0.003 && Math.abs(yVel - travel_d / 16) < 0.003) {
                return -0.003;
            } else {
                return yVel - travel_d / 16;
            }
        }
        return yVelocity;
    }

}
