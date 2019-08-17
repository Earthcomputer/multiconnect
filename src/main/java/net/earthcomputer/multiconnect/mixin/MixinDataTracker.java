package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.DataTrackerManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.util.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DataTracker.class)
public class MixinDataTracker {

    private int nextAbsentId = -1;

    @Inject(method = "registerData", at = @At("RETURN"))
    private static <T> void onRegisterData(Class<? extends Entity> clazz, TrackedDataHandler<T> dataType, CallbackInfoReturnable<TrackedData<T>> ci) {
        DataTrackerManager.onRegisterData(clazz, ci.getReturnValue());
    }

    @Inject(method = "addTrackedData", at = @At("HEAD"), cancellable = true)
    public <T> void onAddTrackedData(TrackedData<T> data, T _default, CallbackInfo ci) {
        DataTrackerManager.onCreateDataEntry();
        if (data.getId() == DataTrackerManager.DEFAULT_ABSENT_ID)
            DataTrackerManager.setId(data, nextAbsentId--);
    }

    @Inject(method = "writeEntryToPacket", at = @At("HEAD"), cancellable = true)
    private static <T> void onWriteEntryToPacket(PacketByteBuf buf, DataTracker.Entry<T> entry, CallbackInfo ci) {
        if (entry.getData().getId() < 0)
            ci.cancel();
    }

}
