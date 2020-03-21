package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.DataTrackerManager;
import net.earthcomputer.multiconnect.impl.IDataTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(DataTracker.class)
public abstract class MixinDataTracker implements IDataTracker {

    @Shadow @Final private Map<Integer, DataTracker.Entry<?>> entries;

    @Shadow protected abstract <T> void addTrackedData(TrackedData<T> trackedData_1, T object_1);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(Entity entity, CallbackInfo ci) {
        DataTrackerManager.addTrackerInstance((DataTracker) (Object) this);
    }

    @Inject(method = "registerData", at = @At("RETURN"))
    private static <T> void onRegisterData(Class<? extends Entity> clazz, TrackedDataHandler<T> dataType, CallbackInfoReturnable<TrackedData<T>> ci) {
        DataTrackerManager.onRegisterData(clazz, ci.getReturnValue());
    }

    @Inject(method = "startTracking", at = @At(value = "INVOKE", target = "Ljava/lang/IllegalArgumentException;<init>(Ljava/lang/String;)V", ordinal = 2, remap = false), cancellable = true)
    private <T> void allowUnregisteredTracker(TrackedData<T> data, T _default, CallbackInfo ci) {
        if (data.getId() < 0) {
            ci.cancel();
            addTrackedData(data, _default);
        }
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

    @Redirect(method = "entryFromPacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/data/TrackedDataHandler;read(Lnet/minecraft/network/PacketByteBuf;)Ljava/lang/Object;"))
    private static <T> T read(TrackedDataHandler<T> handler, PacketByteBuf buf) {
        return ConnectionInfo.protocol.readTrackedData(handler, buf);
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

    @SuppressWarnings("unchecked")
    @Override
    public <T> TrackedData<T> multiconnect_getActualTrackedData(TrackedData<T> data) {
        return (TrackedData<T>) entries.get(data.getId()).getData();
    }
}
