package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
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
    @Shadow protected Object2DoubleMap<Tag<Fluid>> waterHeight;

    @Shadow public abstract Box getBoundingBox();

    @Shadow public abstract void setVelocity(Vec3d velocity);

    @Shadow public abstract Vec3d getVelocity();

    @Inject(method = "setSwimming", at = @At("HEAD"), cancellable = true)
    private void onSetSwimming(boolean swimming, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2 && swimming)
            ci.cancel();
    }

    @Inject(method = "updateMovementInFluid", at = @At("HEAD"), cancellable = true)
    private void modifyFluidMovementBoundingBox(Tag<Fluid> fluidTag, double d, CallbackInfoReturnable<Boolean> ci) {
        if (ConnectionInfo.protocolVersion > Protocols.V1_12_2)
            return;

        Box box = getBoundingBox().expand(0, -0.4, 0).contract(0.001);
        int minX = MathHelper.floor(box.x1);
        int maxX = MathHelper.ceil(box.x2);
        int minY = MathHelper.floor(box.y1);
        int maxY = MathHelper.ceil(box.y2);
        int minZ = MathHelper.floor(box.z1);
        int maxZ = MathHelper.ceil(box.z2);

        if (!world.isRegionLoaded(minX, minY, minZ, maxX, maxY, maxZ))
            ci.setReturnValue(false);

        double waterHeight = 0;
        boolean foundFluid = false;
        Vec3d pushVec = Vec3d.ZERO;

        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (int x = minX; x < maxX; x++) {
            for (int y = minY - 1; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    mutable.set(x, y, z);
                    FluidState state = world.getFluidState(mutable);
                    if (state.matches(fluidTag)) {
                        double height = y + state.getHeight(world, mutable);
                        if (height >= box.y1 - 0.4)
                            waterHeight = Math.max(height - box.y1 + 0.4, waterHeight);
                        if (y >= minY && maxY >= height) {
                            foundFluid = true;
                            pushVec = pushVec.add(state.getVelocity(world, mutable));
                        }
                    }
                }
            }
        }

        if (pushVec.length() > 0) {
            pushVec = pushVec.normalize().multiply(0.014);
            setVelocity(getVelocity().add(pushVec));
        }

        this.waterHeight.put(fluidTag, waterHeight);
        ci.setReturnValue(foundFluid);
    }

}
