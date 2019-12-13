package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.DataTrackerManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class MixinEntity {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(EntityType<?> type, World world, CallbackInfo ci) {
        DataTrackerManager.postRegisterData(((Entity) (Object) this).getClass());
        DataTrackerManager.startTrackingOldTrackedData((Entity) (Object) this);
    }

    @Inject(method = "onTrackedDataSet", at = @At("HEAD"))
    private void onOnTrackedDataSet(TrackedData<?> data, CallbackInfo ci) {
        DataTrackerManager.handleOldTrackedData((Entity) (Object) this, data);
    }

}
