package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.DataTrackerManager;
import net.earthcomputer.multiconnect.impl.IDataTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.util.PacketByteBuf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(DataTracker.class)
public class MixinDataTracker implements IDataTracker {

    @Shadow @Final private Map<Integer, DataTracker.Entry<?>> entries;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(Entity entity, CallbackInfo ci) {
        DataTrackerManager.addTrackerInstance((DataTracker) (Object) this);
    }

    @Inject(method = "registerData", at = @At("RETURN"))
    private static <T> void onRegisterData(Class<? extends Entity> clazz, TrackedDataHandler<T> dataType, CallbackInfoReturnable<TrackedData<T>> ci) {
        DataTrackerManager.onRegisterData(clazz, ci.getReturnValue());
    }

    @Inject(method = "startTracking", at = @At("HEAD"), cancellable = true)
    public <T> void onStartTracking(TrackedData<T> data, T _default, CallbackInfo ci) {
        DataTrackerManager.onCreateDataEntry();
    }

    @Inject(method = "writeEntryToPacket", at = @At("HEAD"), cancellable = true)
    private static <T> void onWriteEntryToPacket(PacketByteBuf buf, DataTracker.Entry<T> entry, CallbackInfo ci) {
        if (entry.getData().getId() < 0)
            ci.cancel();
    }

    @Override
    public void multiconnect_recomputeEntries() {
        if (entries == null)
            return;
        List<DataTracker.Entry<?>> entryList = new ArrayList<>(entries.values());
        entries.clear();
        for (DataTracker.Entry entry : entryList) {
            entries.put(entry.getData().getId(), entry);
        }
    }
}
