package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Shadow public World world;
    @Shadow protected double submergedHeight;

    @Shadow public abstract AxisAlignedBB getBoundingBox();

    @Shadow public abstract void setMotion(Vec3d velocity);

    @Shadow public abstract Vec3d getMotion();

    @Shadow public abstract double getSubmergedHeight();

    @Shadow public abstract boolean areEyesInFluid(Tag<Fluid> fluidTag, boolean requireLoadedChunk);

    @Inject(method = "setSwimming", at = @At("HEAD"), cancellable = true)
    private void onSetSwimming(boolean swimming, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2 && swimming)
            ci.cancel();
    }

    @Inject(method = "handleFluidAcceleration", at = @At("HEAD"), cancellable = true)
    private void modifyFluidMovementBoundingBox(Tag<Fluid> fluidTag, CallbackInfoReturnable<Boolean> ci) {
        if (ConnectionInfo.protocolVersion > Protocols.V1_12_2)
            return;

        AxisAlignedBB box = getBoundingBox().expand(0, -0.4, 0).contract(0.001, 0.001, 0.001);
        int minX = MathHelper.floor(box.minX);
        int maxX = MathHelper.ceil(box.maxX);
        int minY = MathHelper.floor(box.minY);
        int maxY = MathHelper.ceil(box.maxY);
        int minZ = MathHelper.floor(box.minZ);
        int maxZ = MathHelper.ceil(box.maxZ);

        if (!world.isAreaLoaded(minX, minY, minZ, maxX, maxY, maxZ))
            ci.setReturnValue(false);

        double waterHeight = 0;
        boolean foundFluid = false;
        Vec3d pushVec = Vec3d.ZERO;

        try (BlockPos.PooledMutable pos = BlockPos.PooledMutable.retain()) {
            for (int x = minX; x < maxX; x++) {
                for (int y = minY - 1; y < maxY; y++) {
                    for (int z = minZ; z < maxZ; z++) {
                        pos.setPos(x, y, z);
                        IFluidState state = world.getFluidState(pos);
                        if (state.isTagged(fluidTag)) {
                            double height = y + state.getActualHeight(world, pos);
                            if (height >= box.minY - 0.4)
                                waterHeight = Math.max(height - box.minY + 0.4, waterHeight);
                            if (y >= minY && maxY >= height) {
                                foundFluid = true;
                                pushVec = pushVec.add(state.getFlow(world, pos));
                            }
                        }
                    }
                }
            }
        }

        if (pushVec.length() > 0) {
            pushVec = pushVec.normalize().mul(0.014, 0.014, 0.014);
            setMotion(getMotion().add(pushVec));
        }

        this.submergedHeight = waterHeight;
        ci.setReturnValue(foundFluid);
    }

    @Inject(method = "isInWater", at = @At(value = "RETURN"), cancellable = true)
    private void correctStatus(CallbackInfoReturnable<Boolean> cir) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2 && cir.getReturnValueZ() && !areEyesInFluid(FluidTags.WATER, true) && getSubmergedHeight() < 0.28888931871D) {
            cir.setReturnValue(false);
        }
    }
}
