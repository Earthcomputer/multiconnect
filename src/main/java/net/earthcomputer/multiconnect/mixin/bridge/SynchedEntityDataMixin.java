package net.earthcomputer.multiconnect.mixin.bridge;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.debug.DebugUtils;
import net.earthcomputer.multiconnect.protocols.generic.ISynchedEntityData;
import net.earthcomputer.multiconnect.protocols.generic.SynchedDataManager;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
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

@Mixin(SynchedEntityData.class)
public abstract class SynchedEntityDataMixin implements ISynchedEntityData {

    @Shadow @Final @Mutable
    private Entity entity;
    @Shadow @Final private Int2ObjectMap<SynchedEntityData.DataItem<?>> itemsById;
    @Shadow @Final private ReadWriteLock lock;
    @Shadow private boolean isDirty;

    @Shadow protected abstract <T> void createDataItem(EntityDataAccessor<T> dataAccessor, T item);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(Entity entity, CallbackInfo ci) {
        SynchedDataManager.addTrackerInstance((SynchedEntityData) (Object) this);
    }

    @Inject(method = "defineId", at = @At("RETURN"))
    private static <T> void onDefineId(Class<? extends Entity> clazz, EntityDataSerializer<T> dataType, CallbackInfoReturnable<EntityDataAccessor<T>> ci) {
        SynchedDataManager.onDefineId(clazz, ci.getReturnValue());
    }

    @Inject(method = "define", at = @At(value = "INVOKE", target = "Ljava/lang/IllegalArgumentException;<init>(Ljava/lang/String;)V", ordinal = 2, remap = false), cancellable = true)
    private <T> void allowUnregisteredTracker(EntityDataAccessor<T> data, T _default, CallbackInfo ci) {
        if (data.getId() < 0) {
            ci.cancel();
            createDataItem(data, _default);
        }
    }

    @Inject(method = "define", at = @At("HEAD"))
    public <T> void onDefine(EntityDataAccessor<T> data, T _default, CallbackInfo ci) {
        SynchedDataManager.onDefineDataItem();
    }

    @Inject(method = "writeDataItem", at = @At("HEAD"), cancellable = true)
    private static <T> void onWriteDataItem(FriendlyByteBuf buf, SynchedEntityData.DataItem<T> entry, CallbackInfo ci) {
        if (entry.getAccessor().getId() < 0)
            ci.cancel();
    }

    @Redirect(method = "genericHelper", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/syncher/EntityDataSerializer;read(Lnet/minecraft/network/FriendlyByteBuf;)Ljava/lang/Object;"))
    private static <T> T read(EntityDataSerializer<T> handler, FriendlyByteBuf buf) {
        return ConnectionInfo.protocol.readEntityData(handler, buf);
    }

    @Inject(method = "getItem", at = @At("RETURN"))
    private void assertCorrectType(EntityDataAccessor<?> requested, CallbackInfoReturnable<SynchedEntityData.DataItem<?>> ci) {
        if (ci.getReturnValue() == null) {
            throw new IllegalStateException("getEntry returned null for ID %d(%s) for entity %s!\n%s".formatted(
                    requested.getId(),
                    DebugUtils.getEntityDataName(requested),
                    Registry.ENTITY_TYPE.getKey(entity.getType()),
                    DebugUtils.getAllEntityData(entity)));
        }
        if (ci.getReturnValue().getAccessor().getSerializer() != requested.getSerializer()) {
            throw new IllegalStateException("getEntry returned wrong type for ID %d(%s) for entity %s!\n%s".formatted(
                    requested.getId(),
                    DebugUtils.getEntityDataName(requested),
                    Registry.ENTITY_TYPE.getKey(entity.getType()),
                    DebugUtils.getAllEntityData(entity)));
        }
    }

    @Inject(method = "assignValue", at = @At(value = "INVOKE", target = "Ljava/lang/IllegalStateException;<init>(Ljava/lang/String;)V", remap = false))
    private void improveErrorMessage(SynchedEntityData.DataItem<?> entry, SynchedEntityData.DataItem<?> entry2, CallbackInfo ci) {
        throw new IllegalStateException(
                "Invalid entity data item type for field %d(%s) on entity %s: into=%s(%s), from=%s(%s)\n%s".formatted(
                entry.getAccessor().getId(),
                DebugUtils.getEntityDataName(entry.getAccessor()),
                Registry.ENTITY_TYPE.getKey(entity.getType()),
                entry.getValue(),
                entry.getValue().getClass(),
                entry2.getValue(),
                entry2.getValue().getClass(),
                DebugUtils.getAllEntityData(entity)));
    }

    @Override
    public void multiconnect_recomputeItems() {
        if (itemsById == null)
            return;
        var entryList = new ArrayList<>(itemsById.values());
        itemsById.clear();
        for (SynchedEntityData.DataItem<?> entry : entryList) {
            itemsById.put(entry.getAccessor().getId(), entry);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> EntityDataAccessor<T> multiconnect_getActualData(EntityDataAccessor<T> data) {
        return (EntityDataAccessor<T>) itemsById.get(data.getId()).getAccessor();
    }

    @Override
    public void multiconnect_setEntityTo(Entity entity) {
        lock.writeLock().lock();
        this.entity = entity;
        //noinspection ConstantConditions
        var otherEntries = ((SynchedEntityDataMixin) (Object) entity.getEntityData()).itemsById.values();
        var oldThisEntries = new HashMap<>(this.itemsById);
        this.itemsById.clear();
        for (SynchedEntityData.DataItem<?> otherEntry : otherEntries) {
            int dataId = otherEntry.getAccessor().getId();
            SynchedEntityData.DataItem<?> entry = oldThisEntries.getOrDefault(dataId, otherEntry);
            itemsById.put(dataId, entry);
            entry.setDirty(true);
        }
        isDirty = true;
        lock.writeLock().unlock();
    }
}
