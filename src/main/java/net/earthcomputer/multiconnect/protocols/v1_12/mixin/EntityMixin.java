package net.earthcomputer.multiconnect.protocols.v1_12.mixin;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow public Level level;
    @Shadow protected Object2DoubleMap<TagKey<Fluid>> fluidHeight;

    @Shadow public abstract AABB getBoundingBox();

    @Shadow public abstract void setDeltaMovement(Vec3 velocity);

    @Shadow public abstract Vec3 getDeltaMovement();

    @Inject(method = "setSwimming", at = @At("HEAD"), cancellable = true)
    private void onSetSwimming(boolean swimming, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2 && swimming)
            ci.cancel();
    }

    @SuppressWarnings("deprecation")
    @Inject(method = "updateFluidHeightAndDoFluidPushing", at = @At("HEAD"), cancellable = true)
    private void modifyFluidMovementBoundingBox(TagKey<Fluid> fluidTag, double d, CallbackInfoReturnable<Boolean> ci) {
        if (ConnectionInfo.protocolVersion > Protocols.V1_12_2)
            return;

        AABB box = getBoundingBox().inflate(0, -0.4, 0).deflate(0.001);
        int minX = Mth.floor(box.minX);
        int maxX = Mth.ceil(box.maxX);
        int minY = Mth.floor(box.minY);
        int maxY = Mth.ceil(box.maxY);
        int minZ = Mth.floor(box.minZ);
        int maxZ = Mth.ceil(box.maxZ);

        if (!level.hasChunksAt(minX, minY, minZ, maxX, maxY, maxZ))
            ci.setReturnValue(false);

        double waterHeight = 0;
        boolean foundFluid = false;
        Vec3 pushVec = Vec3.ZERO;

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int x = minX; x < maxX; x++) {
            for (int y = minY - 1; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    mutable.set(x, y, z);
                    FluidState state = level.getFluidState(mutable);
                    if (state.is(fluidTag)) {
                        double height = y + state.getHeight(level, mutable);
                        if (height >= box.minY - 0.4)
                            waterHeight = Math.max(height - box.minY + 0.4, waterHeight);
                        if (y >= minY && maxY >= height) {
                            foundFluid = true;
                            pushVec = pushVec.add(state.getFlow(level, mutable));
                        }
                    }
                }
            }
        }

        if (pushVec.length() > 0) {
            pushVec = pushVec.normalize().scale(0.014);
            setDeltaMovement(getDeltaMovement().add(pushVec));
        }

        this.fluidHeight.put(fluidTag, waterHeight);
        ci.setReturnValue(foundFluid);
    }

}
