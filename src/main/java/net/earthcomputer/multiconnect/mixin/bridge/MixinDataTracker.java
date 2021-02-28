package net.earthcomputer.multiconnect.mixin.bridge;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.generic.DataTrackerManager;
import net.earthcomputer.multiconnect.protocols.generic.IDataTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;

@Mixin(DataTracker.class)
public abstract class MixinDataTracker implements IDataTracker {

    @Shadow @Final @Mutable
    private Entity trackedEntity;
    @Shadow @Final private Int2ObjectMap<DataTracker.Entry<?>> entries;
    @Shadow @Final private ReadWriteLock lock;
    @Shadow private boolean dirty;

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

    @Inject(method = "startTracking", at = @At("HEAD"))
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

    @Inject(method = "getEntry", at = @At("RETURN"))
    private void assertCorrectType(TrackedData<?> requested, CallbackInfoReturnable<DataTracker.Entry<?>> ci) {
        if (ci.getReturnValue() == null) {
            throw new AssertionError("getEntry returned null for ID " + requested.getId() + " for entity " + Registry.ENTITY_TYPE.getId(trackedEntity.getType()) + "!");
        }
        if (ci.getReturnValue().getData().getType() != requested.getType()) {
            throw new AssertionError("getEntry returned wrong type for ID " + requested.getId() + " for entity " + Registry.ENTITY_TYPE.getId(trackedEntity.getType()) + "!");
        }
    }

    @Override
    public void multiconnect_recomputeEntries() {
        if (entries == null)
            return;
        List<DataTracker.Entry<?>> entryList = new ArrayList<>(entries.values());
        entries.clear();
        for (DataTracker.Entry<?> entry : entryList) {
            entries.put(entry.getData().getId(), entry);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> TrackedData<T> multiconnect_getActualTrackedData(TrackedData<T> data) {
        return (TrackedData<T>) entries.get(data.getId()).getData();
    }

    @Override
    public void multiconnect_setEntityTo(Entity entity) {
        lock.writeLock().lock();
        this.trackedEntity = entity;
        //noinspection ConstantConditions
        Collection<DataTracker.Entry<?>> otherEntries = ((MixinDataTracker) (Object) entity.getDataTracker()).entries.values();
        Map<Integer, DataTracker.Entry<?>> oldThisEntries = new HashMap<>(this.entries);
        this.entries.clear();
        for (DataTracker.Entry<?> otherEntry : otherEntries) {
            int dataId = otherEntry.getData().getId();
            DataTracker.Entry<?> entry = oldThisEntries.getOrDefault(dataId, otherEntry);
            entries.put(dataId, entry);
            entry.setDirty(true);
        }
        dirty = true;
        lock.writeLock().unlock();
    }
}
