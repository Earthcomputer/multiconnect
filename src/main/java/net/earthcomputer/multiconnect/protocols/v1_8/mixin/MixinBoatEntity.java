package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_8.IBoatEntity_1_8;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoatEntity.class)
public abstract class MixinBoatEntity extends Entity implements IBoatEntity_1_8 {
    @Shadow private double x;
    @Shadow private double y;
    @Shadow private double z;
    @Shadow private double boatYaw;
    @Shadow private double boatPitch;

    @Shadow public abstract int getDamageWobbleTicks();

    @Shadow public abstract void setDamageWobbleTicks(int wobbleTicks);

    @Shadow public abstract float getDamageWobbleStrength();

    @Shadow public abstract void setDamageWobbleStrength(float wobbleStrength);

    @Shadow @Nullable public abstract Entity getPrimaryPassenger();

    @Shadow private BoatEntity.Location location;
    @Unique private boolean isBoatEmpty = true;
    @Unique private double speedMultiplier = 0.07;
    @Unique private int boatPosRotationIncrements;
    @Unique private double velocityX;
    @Unique private double velocityY;
    @Unique private double velocityZ;

    public MixinBoatEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "getMountedHeightOffset", at = @At("HEAD"), cancellable = true)
    private void onGetMountedHeightOffset(CallbackInfoReturnable<Double> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            ci.setReturnValue(-0.3);
        }
    }

    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    private void onPushAwayFrom(Entity entity, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            super.pushAwayFrom(entity);
            ci.cancel();
        }
    }

    @Inject(method = "updateTrackedPositionAndAngles", at = @At("HEAD"), cancellable = true)
    private void onUpdateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            if (interpolate && hasPassengers()) {
                this.prevX = x;
                this.prevY = y;
                this.prevZ = z;
                this.boatPosRotationIncrements = 0;
                setPosition(x, y, z);
                setRotation(yaw, pitch);
                setVelocity(Vec3d.ZERO);
                velocityX = velocityY = velocityZ = 0;
            } else {
                if (isBoatEmpty) {
                    boatPosRotationIncrements = interpolationSteps + 5;
                } else {
                    if (squaredDistanceTo(x, y, z) <= 1) {
                        return;
                    }
                    boatPosRotationIncrements = 3;
                }

                this.x = x;
                this.y = y;
                this.z = z;
                this.boatYaw = yaw;
                this.boatPitch = pitch;
                setVelocity(velocityX, velocityY, velocityZ);
            }
            ci.cancel();
        }
    }

    @Override
    public void setVelocityClient(double x, double y, double z) {
        super.setVelocityClient(x, y, z);
        velocityX = x;
        velocityY = y;
        velocityZ = z;
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            super.tick();

            if (getDamageWobbleTicks() > 0) {
                setDamageWobbleTicks(getDamageWobbleTicks() - 1);
            }
            if (getDamageWobbleStrength() > 0) {
                setDamageWobbleStrength(getDamageWobbleStrength() - 1);
            }
            prevX = getX();
            prevY = getY();
            prevZ = getZ();

            // calculate how submerged in water the boat is
            final int yPartitions = 5;
            double percentSubmerged = 0;
            for (int partitionIndex = 0; partitionIndex < yPartitions; partitionIndex++) {
                double minY = getBoundingBox().minY + getBoundingBox().getYLength() * partitionIndex / yPartitions - 0.125;
                double maxY = getBoundingBox().minY + getBoundingBox().getYLength() * (partitionIndex+1) / yPartitions - 0.125;
                Box box = new Box(getBoundingBox().minX, minY, getBoundingBox().minZ, getBoundingBox().maxX, maxY, getBoundingBox().maxZ);
                if (BlockPos.stream(box).anyMatch(pos -> world.getFluidState(pos).isIn(FluidTags.WATER))) {
                    percentSubmerged += 1.0 / yPartitions;
                }
            }

            // spawn boat movement splash particles
            double oldHorizontalSpeed = Math.sqrt(getVelocity().x * getVelocity().x + getVelocity().z * getVelocity().z);
            if (oldHorizontalSpeed > 0.2975) {
                double rx = Math.cos(yaw * Math.PI / 180);
                double rz = Math.sin(yaw * Math.PI / 180);
                for (int i = 0; i < 1 + oldHorizontalSpeed * 60; i++) {
                    double dForward = random.nextFloat() * 2 - 1;
                    double dSideways = (random.nextInt(2) * 2 - 1) * 0.7;
                    if (random.nextBoolean()) {
                        // particles on the side of the boat
                        double x = getX() - rx * dForward * 0.8 + rz * dSideways;
                        double z = getZ() - rz * dForward * 0.8 - rx * dSideways;
                        world.addParticle(ParticleTypes.SPLASH, x, getY() - 0.125, z, getVelocity().x, getVelocity().y, getVelocity().z);
                    } else {
                        // particles trailing behind the boat
                        double x = getX() + rx + rz * dForward * 0.7;
                        double z = getZ() + rz - rx * dForward * 0.7;
                        world.addParticle(ParticleTypes.SPLASH, x, getY() - 0.125, z, getVelocity().x, getVelocity().y, getVelocity().z);
                    }
                }
            }

            if (isBoatEmpty) {
                if (boatPosRotationIncrements > 0) {
                    double newX = getX() + (this.x - getX()) / boatPosRotationIncrements;
                    double newY = getY() + (this.y - getY()) / boatPosRotationIncrements;
                    double newZ = getZ() + (this.z - getZ()) / boatPosRotationIncrements;
                    double newYaw = this.yaw + (this.boatYaw - this.yaw) / boatPosRotationIncrements;
                    double newPitch = this.pitch + (this.boatPitch - this.pitch) / boatPosRotationIncrements;
                    boatPosRotationIncrements--;
                    setPosition(newX, newY, newZ);
                    setRotation((float)newYaw, (float)newPitch);
                } else {
                    setPosition(getX() + getVelocity().x, getY() + getVelocity().y, getZ() + getVelocity().z);
                    if (onGround) {
                        setVelocity(getVelocity().multiply(0.5));
                    }
                    setVelocity(getVelocity().multiply(0.99, 0.95, 0.99));
                }
            } else {
                if (percentSubmerged < 1) {
                    double normalizedDistanceFromMiddle = percentSubmerged * 2 - 1;
                    setVelocity(getVelocity().add(0, 0.04 * normalizedDistanceFromMiddle, 0));
                } else {
                    if (getVelocity().y < 0) {
                        setVelocity(getVelocity().multiply(1, 0.5, 1));
                    }
                    setVelocity(getVelocity().add(0, 0.007, 0));
                }

                if (getPrimaryPassenger() instanceof LivingEntity) {
                    LivingEntity passenger = (LivingEntity) getPrimaryPassenger();
                    float boatAngle = passenger.yaw - passenger.sidewaysSpeed * 90;
                    double xAcceleration = -Math.sin(boatAngle * Math.PI / 180) * speedMultiplier * passenger.forwardSpeed * 0.05;
                    double zAcceleration = Math.cos(boatAngle * Math.PI / 180) * speedMultiplier * passenger.forwardSpeed * 0.05;
                    setVelocity(getVelocity().add(xAcceleration, 0, zAcceleration));
                }

                double newHorizontalSpeed = Math.sqrt(getVelocity().x * getVelocity().x + getVelocity().z * getVelocity().z);
                // cap horizontal speed at 0.35
                if (newHorizontalSpeed > 0.35) {
                    double multiplier = 0.35 / newHorizontalSpeed;
                    setVelocity(getVelocity().multiply(multiplier, 1, multiplier));
                    newHorizontalSpeed = 0.35;
                }

                if (newHorizontalSpeed > oldHorizontalSpeed && speedMultiplier < 0.35) {
                    speedMultiplier += (0.35 - speedMultiplier) / 35;
                    if (speedMultiplier > 0.35) {
                        speedMultiplier = 0.35;
                    }
                } else {
                    speedMultiplier -= (speedMultiplier - 0.07) / 35;
                    if (speedMultiplier < 0.07) {
                        speedMultiplier = 0.07;
                    }
                }

                for (int i = 0; i < 4; i++) {
                    int dx = MathHelper.floor(getX() + ((i%2) - 0.5) * 0.8);
                    //noinspection IntegerDivisionInFloatingPointContext
                    int dz = MathHelper.floor(getZ() + ((i/2) - 0.5) * 0.8);
                    for (int ddy = 0; ddy < 2; ddy++) {
                        int dy = MathHelper.floor(getY()) + ddy;
                        BlockPos pos = new BlockPos(dx, dy, dz);
                        Block block = world.getBlockState(pos).getBlock();
                        if (block == Blocks.SNOW) {
                            world.setBlockState(pos, Blocks.AIR.getDefaultState());
                            horizontalCollision = false;
                        } else if (block == Blocks.LILY_PAD) {
                            world.breakBlock(pos, true);
                            horizontalCollision = false;
                        }
                    }
                }

                if (onGround) {
                    setVelocity(getVelocity().multiply(0.5));
                }

                move(MovementType.SELF, getVelocity());

                if (!horizontalCollision || oldHorizontalSpeed <= 0.2975) {
                    setVelocity(getVelocity().multiply(0.99, 0.95, 0.99));
                }

                pitch = 0;
                double deltaX = prevX - getX();
                double deltaZ = prevZ - getZ();
                if (deltaX * deltaX + deltaZ * deltaZ > 0.001) {
                    yaw = MathHelper.stepAngleTowards(yaw, (float)(MathHelper.atan2(deltaZ, deltaX) * 180 / Math.PI), 20);
                }

            }

            ci.cancel();
        }
    }

    @Inject(method = "updatePassengerPosition", at = @At("HEAD"), cancellable = true)
    private void onUpdatePassengerPosition(Entity passenger, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            if (hasPassenger(passenger)) {
                double dx = Math.cos(this.yaw * Math.PI / 180) * 0.4;
                double dz = Math.sin(this.yaw * Math.PI / 180) * 0.4;
                passenger.setPosition(getX() + dx, getY() + getMountedHeightOffset(), getZ() + dz);
            }
            ci.cancel();
        }
    }

    @Inject(method = "onPassengerLookAround", at = @At("HEAD"), cancellable = true)
    private void onOnPassengerLookAround(Entity passenger, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            // don't prevent entities looking around in the boat
            super.onPassengerLookAround(passenger);
            ci.cancel();
        }
    }

    @Inject(method = "fall", at = @At("HEAD"))
    private void onFall(CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            // prevent falling from being negated
            location = BoatEntity.Location.ON_LAND;
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
        this.isBoatEmpty = boatEmpty;
    }
}
