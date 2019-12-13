package net.earthcomputer.multiconnect.impl;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
    private static boolean dirty = false;
    private static int nextAbsentId = -1;
    private static Set<DataTracker> trackerInstances = Collections.newSetFromMap(new WeakHashMap<>());

    private static final Field TRACKED_DATA_ID;
    static {
        try {
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            TRACKED_DATA_ID = Arrays.stream(TrackedData.class.getDeclaredFields())
                    .filter(field -> field.getType() == int.class)
                    .findAny().orElseThrow(NoSuchFieldException::new);
            TRACKED_DATA_ID.setAccessible(true);
            modifiers.set(TRACKED_DATA_ID, TRACKED_DATA_ID.getModifiers() & ~Modifier.FINAL);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    public static void addTrackerInstance(DataTracker instance) {
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
            setId(data, id);
            oldTrackedData.computeIfAbsent(clazz, k -> new ArrayList<>()).add(Pair.of(data, _default));
            oldTrackedDataHandlers.put(data, handler);
        } else {
            setId(data, nextAbsentId--);
        }
    }

    public static void setId(TrackedData<?> data, int id) {
        try {
            TRACKED_DATA_ID.set(data, id);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static int getNextId(Class<? extends Entity> clazz) {
        return NEXT_IDS.computeIfAbsent(clazz, k -> k == Entity.class ? 0 : getNextId((Class<? extends Entity>) clazz.getSuperclass()));
    }

    public static void onRegisterData(Class<? extends Entity> clazz, TrackedData<?> data) {
        DEFAULT_DATA.computeIfAbsent(clazz, k -> new ArrayList<>()).add(data);
        dirty = true;
    }

    public static void postRegisterData(Class<? extends Entity> clazz) {
        if (!DEFAULT_DATA.containsKey(clazz)) {
            DEFAULT_DATA.put(clazz, Collections.emptyList());
            dirty = true;
        }
    }

    public static void onCreateDataEntry() {
        if (dirty)
            reregisterAll();
    }

    private static void reregisterData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (ConnectionInfo.protocol.acceptEntityData(clazz, data)) {
            int id = getNextId(clazz);
            NEXT_IDS.put(clazz, id + 1);
            setId(data, id);
        } else {
            setId(data, nextAbsentId--);
        }
    }

    public static void onConnectToServer() {
        reregisterAll();
    }

    public static void reregisterAll() {
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

    public static void startTrackingOldTrackedData(Entity entity) {
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

    public static void handleOldTrackedData(Entity entity, TrackedData<?> data) {
        data = ((IDataTracker) entity.getDataTracker()).multiconnect_getActualTrackedData(data);
        BiConsumer<?, ?> handler = oldTrackedDataHandlers.get(data);
        if (handler != null)
            doHandleTrackedData(handler, entity, entity.getDataTracker().get(data));
    }

    @SuppressWarnings("unchecked")
    private static <T, U> void doHandleTrackedData(BiConsumer<U, T> handler, Entity entity, Object val) {
        handler.accept((U) entity, (T) val);
    }

    public static Field getTrackedDataField(Class<? extends Entity> clazz, int index, String nameForDebug) {
        try {
            Field field = Arrays.stream(clazz.getDeclaredFields())
                    .filter(f -> f.getType() == TrackedData.class)
                    .skip(index).findFirst().orElseThrow(NoSuchFieldException::new);
            field.setAccessible(true);
            if (FabricLoader.getInstance().isDevelopmentEnvironment())
                if (!field.getName().equals(nameForDebug))
                    throw new AssertionError("Field name " + field.getName() + " does not match " + nameForDebug);
            return field;
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> TrackedData<T> getTrackedData(Class<T> type, Field trackedDataField) {
        try {
            return (TrackedData<T>) trackedDataField.get(null);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

}
