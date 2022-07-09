package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.protocols.generic.SynchedDataManager;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(EntityType<?> type, Level world, CallbackInfo ci) {
        SynchedDataManager.postRegisterData(((Entity) (Object) this).getClass());
        SynchedDataManager.defineIdsForOldData((Entity) (Object) this);
    }

    @Inject(method = "onSyncedDataUpdated", at = @At("HEAD"))
    private void onOnSynchedDataUpdated(EntityDataAccessor<?> data, CallbackInfo ci) {
        SynchedDataManager.handleOldData((Entity) (Object) this, data);
    }

}
