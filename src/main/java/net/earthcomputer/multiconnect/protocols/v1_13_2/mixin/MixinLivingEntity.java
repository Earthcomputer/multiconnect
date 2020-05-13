package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {

    @Shadow protected boolean jumping;

    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Redirect(method = "method_26318", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;jumping:Z"))
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

    @ModifyVariable(method = "method_26317(DZLnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;", ordinal = 0, at = @At("HEAD"))
    private boolean modifyMovingDown(boolean movingDown) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            return true;
        }
        return movingDown;
    }

}
