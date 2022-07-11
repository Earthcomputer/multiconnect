package net.earthcomputer.multiconnect.protocols.v1_14.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow private Vec3 position;

    @Shadow public abstract AABB getBoundingBox();

    @Inject(method = "getBlockPosBelowThatAffectsMyMovement", at = @At("HEAD"), cancellable = true)
    private void onGetBlockPosBelowThatAffectsMyMovement(CallbackInfoReturnable<BlockPos> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_14_4) {
            ci.setReturnValue(new BlockPos(position.x, getBoundingBox().minY - 1, position.z));
        }
    }

}
