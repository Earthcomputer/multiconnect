package net.earthcomputer.multiconnect.impl;

import net.earthcomputer.multiconnect.mixin.DataParameterAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.datasync.IDataSerializer;
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

    private static Map<Class<? extends Entity>, List<DataParameter<?>>> DEFAULT_DATA = new HashMap<>();
    private static Map<Class<? extends Entity>, Integer> NEXT_IDS = new HashMap<>();
    private static Map<Class<? extends Entity>, List<Pair<DataParameter<?>, ?>>> oldDataParameter = new HashMap<>();
    private static Map<DataParameter<?>, BiConsumer<?, ?>> oldIDataSerializers = new IdentityHashMap<>();
    private static volatile boolean dirty = false;
    private static int nextAbsentId = -1;
    private static Set<EntityDataManager> trackerInstances = Collections.newSetFromMap(new WeakHashMap<>());

    public static synchronized void addTrackerInstance(EntityDataManager instance) {
        trackerInstances.add(instance);
    }

    public static <T> DataParameter<T> createOldDataParameter(IDataSerializer<T> type) {
        return type.createKey(-1);
    }

    /**
     * Called from inside a protocol's acceptEntityData or postEntityDataRegister method to register an old
     * tracked data no longer in the current version. Will insert the new data before the current data being
     * checked.
     */
    public static <T, U extends Entity> void registerOldDataParameter(Class<U> clazz, DataParameter<T> data, T _default, BiConsumer<? super U, ? super T> handler) {
        if (ConnectionInfo.protocol.acceptEntityData(clazz, data)) {
            int id = getNextFreeId(clazz);
            NEXT_IDS.put(clazz, id + 1);
            ((DataParameterAccessor) data).setId(id);
            oldDataParameter.computeIfAbsent(clazz, k -> new ArrayList<>()).add(Pair.of(data, _default));
            oldIDataSerializers.put(data, handler);
        } else {
            ((DataParameterAccessor) data).setId(nextAbsentId--);
        }
    }

    @SuppressWarnings("unchecked")
    private static int getNextFreeId(Class<? extends Entity> clazz) {
        Integer ret = NEXT_IDS.get(clazz);
        if (ret == null) {
            int nextId = clazz == Entity.class ? 0 : getNextFreeId((Class<? extends Entity>) clazz.getSuperclass());
            NEXT_IDS.put(clazz, nextId);
            return nextId;
        }
        return ret;
    }

    public static synchronized void onRegisterData(Class<? extends Entity> clazz, DataParameter<?> data) {
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

    private static void reregisterData(Class<? extends Entity> clazz, DataParameter<?> data) {
        if (ConnectionInfo.protocol.acceptEntityData(clazz, data)) {
            int id = getNextFreeId(clazz);
            NEXT_IDS.put(clazz, id + 1);
            ((DataParameterAccessor) data).setId(id);
        } else {
            ((DataParameterAccessor) data).setId(nextAbsentId--);
        }
    }

    public static void onConnectToServer() {
        reregisterAll();
    }

    private static void reregisterAll() {
        nextAbsentId = -1;
        NEXT_IDS.clear();
        oldDataParameter.clear();
        oldIDataSerializers.clear();
        Set<Class<? extends Entity>> alreadyReregistered = new HashSet<>();
        for (Class<? extends Entity> clazz : DEFAULT_DATA.keySet()) {
            reregisterDataForClass(clazz, alreadyReregistered);
        }
        for (EntityDataManager tracker : trackerInstances) {
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
            for (DataParameter<?> data : DEFAULT_DATA.get(clazz))
                reregisterData(clazz, data);
        ConnectionInfo.protocol.postEntityDataRegister(clazz);
    }

    public static synchronized void startTrackingOldDataParameter(Entity entity) {
        for (Class<?> clazz = entity.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
            List<Pair<DataParameter<?>, ?>> trackedData = oldDataParameter.get(clazz);
            if (trackedData != null) {
                for (Pair<DataParameter<?>, ?> pair : trackedData) {
                    doStartTracking(entity, pair.getLeft(), pair.getRight());
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void doStartTracking(Entity entity, DataParameter<T> data, Object _default) {
        entity.getDataManager().register(data, (T) _default);
    }

    public static synchronized void handleOldDataParameter(Entity entity, DataParameter<?> data) {
        data = ((IDataTracker) entity.getDataManager()).multiconnect_getActualDataParameter(data);
        BiConsumer<?, ?> handler = oldIDataSerializers.get(data);
        if (handler != null)
            doHandleDataParameter(handler, entity, entity.getDataManager().get(data));
    }

    @SuppressWarnings("unchecked")
    private static <T, U> void doHandleDataParameter(BiConsumer<U, T> handler, Entity entity, Object val) {
        handler.accept((U) entity, (T) val);
    }

}
