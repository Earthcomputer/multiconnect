package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.DataTrackerManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.datasync.DataParameter;
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
        DataTrackerManager.startTrackingOldDataParameter((Entity) (Object) this);
    }

    @Inject(method = "notifyDataManagerChange", at = @At("HEAD"))
    private void onOnDataParameterSet(DataParameter<?> data, CallbackInfo ci) {
        DataTrackerManager.handleOldDataParameter((Entity) (Object) this, data);
    }

}
