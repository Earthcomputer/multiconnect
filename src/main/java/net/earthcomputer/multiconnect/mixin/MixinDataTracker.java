package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.DataTrackerManager;
import net.earthcomputer.multiconnect.impl.IDataTracker;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.datasync.IDataSerializer;
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

@Mixin(EntityDataManager.class)
public abstract class MixinDataTracker implements IDataTracker {

    @Shadow @Final private Map<Integer, EntityDataManager.DataEntry<?>> entries;

    @Shadow protected abstract <T> void setEntry(DataParameter<T> trackedData_1, T object_1);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(Entity entity, CallbackInfo ci) {
        DataTrackerManager.addTrackerInstance((EntityDataManager) (Object) this);
    }

    @Inject(method = "createKey", at = @At("RETURN"))
    private static <T> void onRegisterData(Class<? extends Entity> clazz, IDataSerializer<T> dataType, CallbackInfoReturnable<DataParameter<T>> ci) {
        DataTrackerManager.onRegisterData(clazz, ci.getReturnValue());
    }

    @Inject(method = "register", at = @At(value = "INVOKE", target = "Ljava/lang/IllegalArgumentException;<init>(Ljava/lang/String;)V", ordinal = 2, remap = false), cancellable = true)
    private <T> void allowUnregisteredTracker(DataParameter<T> data, T _default, CallbackInfo ci) {
        if (data.getId() < 0) {
            ci.cancel();
            setEntry(data, _default);
        }
    }

    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    public <T> void onStartTracking(DataParameter<T> data, T _default, CallbackInfo ci) {
        DataTrackerManager.onCreateDataEntry();
    }

    @Inject(method = "writeEntry", at = @At("HEAD"), cancellable = true)
    private static <T> void onWriteEntryToPacket(PacketBuffer buf, EntityDataManager.DataEntry<T> entry, CallbackInfo ci) {
        if (entry.getKey().getId() < 0)
            ci.cancel();
    }

    @Redirect(method = "makeDataEntry", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/datasync/IDataSerializer;read(Lnet/minecraft/network/PacketBuffer;)Ljava/lang/Object;"))
    private static <T> T read(IDataSerializer<T> handler, PacketBuffer buf) {
        return ConnectionInfo.protocol.readDataParameter(handler, buf);
    }

    @Override
    public void multiconnect_recomputeEntries() {
        if (entries == null)
            return;
        List<EntityDataManager.DataEntry<?>> entryList = new ArrayList<>(entries.values());
        entries.clear();
        for (EntityDataManager.DataEntry entry : entryList) {
            entries.put(entry.getKey().getId(), entry);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> DataParameter<T> multiconnect_getActualDataParameter(DataParameter<T> data) {
        return (DataParameter<T>) entries.get(data.getId()).getKey();
    }
}
