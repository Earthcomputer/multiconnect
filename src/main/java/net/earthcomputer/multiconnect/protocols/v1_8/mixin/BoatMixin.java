package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_8.IBoatEntity_1_8;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Boat.class)
public abstract class BoatMixin extends Entity implements IBoatEntity_1_8 {
    @Shadow private double lerpX;
    @Shadow private double lerpY;
    @Shadow private double lerpZ;
    @Shadow private double lerpYRot;
    @Shadow private double lerpXRot;

    @Shadow public abstract int getHurtTime();

    @Shadow public abstract void setHurtTime(int wobbleTicks);

    @Shadow public abstract float getDamage();

    @Shadow public abstract void setDamage(float wobbleStrength);

    @Shadow @Nullable public abstract Entity getControllingPassenger();

    @Shadow private Boat.Status status;
    @Unique private boolean multiconnect_isBoatEmpty = true;
    @Unique private double multiconnect_speedMultiplier = 0.07;
    @Unique private int multiconnect_boatPosRotationIncrements;
    @Unique private double multiconnect_velocityX;
    @Unique private double multiconnect_velocityY;
    @Unique private double multiconnect_velocityZ;

    public BoatMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "getPassengersRidingOffset", at = @At("HEAD"), cancellable = true)
    private void onGetPassengersRidingOffset(CallbackInfoReturnable<Double> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            ci.setReturnValue(-0.3);
        }
    }

    @Inject(method = "push", at = @At("HEAD"), cancellable = true)
    private void onPush(Entity entity, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            super.push(entity);
            ci.cancel();
        }
    }

    @Inject(method = "lerpTo", at = @At("HEAD"), cancellable = true)
    private void onLerpTo(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            if (interpolate && isVehicle()) {
                this.xo = x;
                this.yo = y;
                this.zo = z;
                this.multiconnect_boatPosRotationIncrements = 0;
                setPos(x, y, z);
                setRot(yaw, pitch);
                setDeltaMovement(Vec3.ZERO);
                multiconnect_velocityX = multiconnect_velocityY = multiconnect_velocityZ = 0;
            } else {
                if (multiconnect_isBoatEmpty) {
                    multiconnect_boatPosRotationIncrements = interpolationSteps + 5;
                } else {
                    if (distanceToSqr(x, y, z) <= 1) {
                        return;
                    }
                    multiconnect_boatPosRotationIncrements = 3;
                }

                this.lerpX = x;
                this.lerpY = y;
                this.lerpZ = z;
                this.lerpYRot = yaw;
                this.lerpXRot = pitch;
                setDeltaMovement(multiconnect_velocityX, multiconnect_velocityY, multiconnect_velocityZ);
            }
            ci.cancel();
        }
    }

    @Override
    public void lerpMotion(double x, double y, double z) {
        super.lerpMotion(x, y, z);
        multiconnect_velocityX = x;
        multiconnect_velocityY = y;
        multiconnect_velocityZ = z;
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            super.tick();

            if (getHurtTime() > 0) {
                setHurtTime(getHurtTime() - 1);
            }
            if (getDamage() > 0) {
                setDamage(getDamage() - 1);
            }
            xo = getX();
            yo = getY();
            zo = getZ();

            // calculate how submerged in water the boat is
            final int yPartitions = 5;
            double percentSubmerged = 0;
            for (int partitionIndex = 0; partitionIndex < yPartitions; partitionIndex++) {
                double minY = getBoundingBox().minY + getBoundingBox().getYsize() * partitionIndex / yPartitions - 0.125;
                double maxY = getBoundingBox().minY + getBoundingBox().getYsize() * (partitionIndex+1) / yPartitions - 0.125;
                AABB box = new AABB(getBoundingBox().minX, minY, getBoundingBox().minZ, getBoundingBox().maxX, maxY, getBoundingBox().maxZ);
                if (BlockPos.betweenClosedStream(box).anyMatch(pos -> level.getFluidState(pos).is(FluidTags.WATER))) {
                    percentSubmerged += 1.0 / yPartitions;
                }
            }

            // spawn boat movement splash particles
            double oldHorizontalSpeed = Math.sqrt(getDeltaMovement().x * getDeltaMovement().x + getDeltaMovement().z * getDeltaMovement().z);
            if (oldHorizontalSpeed > 0.2975) {
                double rx = Math.cos(getYRot() * Math.PI / 180);
                double rz = Math.sin(getYRot() * Math.PI / 180);
                for (int i = 0; i < 1 + oldHorizontalSpeed * 60; i++) {
                    double dForward = random.nextFloat() * 2 - 1;
                    double dSideways = (random.nextInt(2) * 2 - 1) * 0.7;
                    if (random.nextBoolean()) {
                        // particles on the side of the boat
                        double x = getX() - rx * dForward * 0.8 + rz * dSideways;
                        double z = getZ() - rz * dForward * 0.8 - rx * dSideways;
                        level.addParticle(ParticleTypes.SPLASH, x, getY() - 0.125, z, getDeltaMovement().x, getDeltaMovement().y, getDeltaMovement().z);
                    } else {
                        // particles trailing behind the boat
                        double x = getX() + rx + rz * dForward * 0.7;
                        double z = getZ() + rz - rx * dForward * 0.7;
                        level.addParticle(ParticleTypes.SPLASH, x, getY() - 0.125, z, getDeltaMovement().x, getDeltaMovement().y, getDeltaMovement().z);
                    }
                }
            }

            if (multiconnect_isBoatEmpty) {
                if (multiconnect_boatPosRotationIncrements > 0) {
                    double newX = getX() + (this.lerpX - getX()) / multiconnect_boatPosRotationIncrements;
                    double newY = getY() + (this.lerpY - getY()) / multiconnect_boatPosRotationIncrements;
                    double newZ = getZ() + (this.lerpZ - getZ()) / multiconnect_boatPosRotationIncrements;
                    double newYaw = this.getYRot() + (this.lerpYRot - this.getYRot()) / multiconnect_boatPosRotationIncrements;
                    double newPitch = this.getXRot() + (this.lerpXRot - this.getXRot()) / multiconnect_boatPosRotationIncrements;
                    multiconnect_boatPosRotationIncrements--;
                    setPos(newX, newY, newZ);
                    setRot((float)newYaw, (float)newPitch);
                } else {
                    setPos(getX() + getDeltaMovement().x, getY() + getDeltaMovement().y, getZ() + getDeltaMovement().z);
                    if (onGround) {
                        setDeltaMovement(getDeltaMovement().scale(0.5));
                    }
                    setDeltaMovement(getDeltaMovement().multiply(0.99, 0.95, 0.99));
                }
            } else {
                if (percentSubmerged < 1) {
                    double normalizedDistanceFromMiddle = percentSubmerged * 2 - 1;
                    setDeltaMovement(getDeltaMovement().add(0, 0.04 * normalizedDistanceFromMiddle, 0));
                } else {
                    if (getDeltaMovement().y < 0) {
                        setDeltaMovement(getDeltaMovement().multiply(1, 0.5, 1));
                    }
                    setDeltaMovement(getDeltaMovement().add(0, 0.007, 0));
                }

                if (getControllingPassenger() instanceof LivingEntity passenger) {
                    float boatAngle = passenger.getYRot() - passenger.xxa * 90;
                    double xAcceleration = -Math.sin(boatAngle * Math.PI / 180) * multiconnect_speedMultiplier * passenger.zza * 0.05;
                    double zAcceleration = Math.cos(boatAngle * Math.PI / 180) * multiconnect_speedMultiplier * passenger.zza * 0.05;
                    setDeltaMovement(getDeltaMovement().add(xAcceleration, 0, zAcceleration));
                }

                double newHorizontalSpeed = Math.sqrt(getDeltaMovement().x * getDeltaMovement().x + getDeltaMovement().z * getDeltaMovement().z);
                // cap horizontal speed at 0.35
                if (newHorizontalSpeed > 0.35) {
                    double multiplier = 0.35 / newHorizontalSpeed;
                    setDeltaMovement(getDeltaMovement().multiply(multiplier, 1, multiplier));
                    newHorizontalSpeed = 0.35;
                }

                if (newHorizontalSpeed > oldHorizontalSpeed && multiconnect_speedMultiplier < 0.35) {
                    multiconnect_speedMultiplier += (0.35 - multiconnect_speedMultiplier) / 35;
                    if (multiconnect_speedMultiplier > 0.35) {
                        multiconnect_speedMultiplier = 0.35;
                    }
                } else {
                    multiconnect_speedMultiplier -= (multiconnect_speedMultiplier - 0.07) / 35;
                    if (multiconnect_speedMultiplier < 0.07) {
                        multiconnect_speedMultiplier = 0.07;
                    }
                }

                for (int i = 0; i < 4; i++) {
                    int dx = Mth.floor(getX() + ((i%2) - 0.5) * 0.8);
                    //noinspection IntegerDivisionInFloatingPointContext
                    int dz = Mth.floor(getZ() + ((i/2) - 0.5) * 0.8);
                    for (int ddy = 0; ddy < 2; ddy++) {
                        int dy = Mth.floor(getY()) + ddy;
                        BlockPos pos = new BlockPos(dx, dy, dz);
                        Block block = level.getBlockState(pos).getBlock();
                        if (block == Blocks.SNOW) {
                            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                            horizontalCollision = false;
                        } else if (block == Blocks.LILY_PAD) {
                            level.destroyBlock(pos, true);
                            horizontalCollision = false;
                        }
                    }
                }

                if (onGround) {
                    setDeltaMovement(getDeltaMovement().scale(0.5));
                }

                move(MoverType.SELF, getDeltaMovement());

                if (!horizontalCollision || oldHorizontalSpeed <= 0.2975) {
                    setDeltaMovement(getDeltaMovement().multiply(0.99, 0.95, 0.99));
                }

                setXRot(0);
                double deltaX = xo - getX();
                double deltaZ = zo - getZ();
                if (deltaX * deltaX + deltaZ * deltaZ > 0.001) {
                    setYRot(Mth.rotateIfNecessary(getYRot(), (float)(Mth.atan2(deltaZ, deltaX) * 180 / Math.PI), 20));
                }

            }

            ci.cancel();
        }
    }

    @Inject(method = "positionRider", at = @At("HEAD"), cancellable = true)
    private void onPositionRider(Entity passenger, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            if (hasPassenger(passenger)) {
                double dx = Math.cos(this.getYRot() * Math.PI / 180) * 0.4;
                double dz = Math.sin(this.getYRot() * Math.PI / 180) * 0.4;
                passenger.setPos(getX() + dx, getY() + getPassengersRidingOffset(), getZ() + dz);
            }
            ci.cancel();
        }
    }

    @Inject(method = "onPassengerTurned", at = @At("HEAD"), cancellable = true)
    private void onOnPassengerTurned(Entity passenger, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            // don't prevent entities looking around in the boat
            super.onPassengerTurned(passenger);
            ci.cancel();
        }
    }

    @Inject(method = "checkFallDamage", at = @At("HEAD"))
    private void onFall(CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            // prevent falling from being negated
            status = Boat.Status.ON_LAND;
        }
    }

    @Inject(method = "canAddPassenger", at = @At("HEAD"), cancellable = true)
    private void onCanAddPassenger(Entity passenger, CallbackInfoReturnable<Boolean> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            // only one entity can ride a boat at a time
            ci.setReturnValue(super.canAddPassenger(passenger));
        }
    }

    @Override
    public void multiconnect_setBoatEmpty(boolean boatEmpty) {
        this.multiconnect_isBoatEmpty = boatEmpty;
    }
}
