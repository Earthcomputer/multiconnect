package net.earthcomputer.multiconnect.protocols.generic;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.mixin.bridge.EntityAccessor;
import net.earthcomputer.multiconnect.mixin.bridge.TrackedDataAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.BiConsumer;

public class DataTrackerManager {
    /*
     * We can't get default data upfront, because that requires the entity class list.
     * Instead, we save the order when vanilla registers the data via DataTracker.registerData.
     * We cannot rewrite the data IDs right at this point, as AbstractProtocol subclasses use
     * the static fields in the entity classes to identify data, but they will still be null
     * because the data has yet to be returned by registerData.
     * Therefore, when registerData is called, an ID rewrite ("reregister") is scheduled by
     * setting the "dirty" flag to true. The reregister happens at the first opportunity, before
     * the first time a data entry is added to an entity instance.
     * Reregisters go through all known entity classes so far which we've collected via our
     * hook in registerData and rewrites the IDs based on the current protocol's
     * acceptEntityData method.
     * Reregisters also happen when connecting to a new server.
     */

    private static Map<Class<? extends Entity>, List<TrackedData<?>>> DEFAULT_DATA = new HashMap<>();
    private static Map<Class<? extends Entity>, Integer> NEXT_IDS = new HashMap<>();
    private static Map<Class<? extends Entity>, List<Pair<TrackedData<?>, ?>>> oldTrackedData = new HashMap<>();
    private static Map<TrackedData<?>, BiConsumer<?, ?>> oldTrackedDataHandlers = new IdentityHashMap<>();
    private static volatile boolean dirty = false;
    private static int nextAbsentId = -1;
    private static Set<DataTracker> trackerInstances = Collections.newSetFromMap(new WeakHashMap<>());

    public static synchronized void addTrackerInstance(DataTracker instance) {
        trackerInstances.add(instance);
    }

    public static <T> TrackedData<T> createOldTrackedData(TrackedDataHandler<T> type) {
        return type.create(-1);
    }

    /**
     * Called from inside a protocol's acceptEntityData or postEntityDataRegister method to register an old
     * tracked data no longer in the current version. Will insert the new data before the current data being
     * checked.
     */
    public static <T, U extends Entity> void registerOldTrackedData(Class<U> clazz, TrackedData<T> data, T _default, BiConsumer<? super U, ? super T> handler) {
        if (ConnectionInfo.protocol.acceptEntityData(clazz, data)) {
            int id = getNextId(clazz);
            NEXT_IDS.put(clazz, id + 1);
            ((TrackedDataAccessor) data).setId(id);
            oldTrackedData.computeIfAbsent(clazz, k -> new ArrayList<>()).add(Pair.of(data, _default));
            oldTrackedDataHandlers.put(data, handler);
        } else {
            ((TrackedDataAccessor) data).setId(nextAbsentId--);
        }
    }

    @SuppressWarnings("unchecked")
    private static int getNextId(Class<? extends Entity> clazz) {
        Integer ret = NEXT_IDS.get(clazz);
        if (ret == null) {
            int nextId = clazz == Entity.class ? 0 : getNextId((Class<? extends Entity>) clazz.getSuperclass());
            NEXT_IDS.put(clazz, nextId);
            return nextId;
        }
        return ret;
    }

    public static synchronized void onRegisterData(Class<? extends Entity> clazz, TrackedData<?> data) {
        DEFAULT_DATA.computeIfAbsent(clazz, k -> new ArrayList<>()).add(data);
        dirty = true;
    }

    public static synchronized void postRegisterData(Class<? extends Entity> clazz) {
        if (!DEFAULT_DATA.containsKey(clazz)) {
            DEFAULT_DATA.put(clazz, Collections.emptyList());
            dirty = true;
        }
    }

    public static synchronized void onCreateDataEntry() {
        if (dirty)
            reregisterAll();
    }

    private static void reregisterData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (ConnectionInfo.protocol.acceptEntityData(clazz, data)) {
            int id = getNextId(clazz);
            NEXT_IDS.put(clazz, id + 1);
            ((TrackedDataAccessor) data).setId(id);
        } else {
            ((TrackedDataAccessor) data).setId(nextAbsentId--);
        }
    }

    public static void onConnectToServer() {
        reregisterAll();
    }

    private static void reregisterAll() {
        nextAbsentId = -1;
        NEXT_IDS.clear();
        oldTrackedData.clear();
        oldTrackedDataHandlers.clear();
        Set<Class<? extends Entity>> alreadyReregistered = new HashSet<>();
        for (Class<? extends Entity> clazz : DEFAULT_DATA.keySet()) {
            reregisterDataForClass(clazz, alreadyReregistered);
        }
        for (DataTracker tracker : trackerInstances) {
            ((IDataTracker) tracker).multiconnect_recomputeEntries();
        }
        dirty = false;
    }

    @SuppressWarnings("unchecked")
    private static void reregisterDataForClass(Class<? extends Entity> clazz, Set<Class<? extends Entity>> alreadyReregistered) {
        if (!alreadyReregistered.add(clazz))
            return;

        if (clazz != Entity.class)
            reregisterDataForClass((Class<? extends Entity>) clazz.getSuperclass(), alreadyReregistered);

        if (DEFAULT_DATA.containsKey(clazz))
            for (TrackedData<?> data : DEFAULT_DATA.get(clazz))
                reregisterData(clazz, data);
        ConnectionInfo.protocol.postEntityDataRegister(clazz);
    }

    public static synchronized void startTrackingOldTrackedData(Entity entity) {
        for (Class<?> clazz = entity.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
            List<Pair<TrackedData<?>, ?>> trackedData = oldTrackedData.get(clazz);
            if (trackedData != null) {
                for (Pair<TrackedData<?>, ?> pair : trackedData) {
                    doStartTracking(entity, pair.getLeft(), pair.getRight());
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void doStartTracking(Entity entity, TrackedData<T> data, Object _default) {
        entity.getDataTracker().startTracking(data, (T) _default);
    }

    public static synchronized void handleOldTrackedData(Entity entity, TrackedData<?> data) {
        data = ((IDataTracker) entity.getDataTracker()).multiconnect_getActualTrackedData(data);
        BiConsumer<?, ?> handler = oldTrackedDataHandlers.get(data);
        if (handler != null)
            doHandleTrackedData(handler, entity, entity.getDataTracker().get(data));
    }

    @SuppressWarnings("unchecked")
    private static <T, U> void doHandleTrackedData(BiConsumer<U, T> handler, Entity entity, Object val) {
        handler.accept((U) entity, (T) val);
    }

    public static void transferDataTracker(Entity oldEntity, Entity newEntity) {
        ((IDataTracker) oldEntity.getDataTracker()).multiconnect_setEntityTo(newEntity);
        ((EntityAccessor) newEntity).setDataTracker(oldEntity.getDataTracker());
    }

}
