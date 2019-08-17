package net.earthcomputer.multiconnect.impl;

import net.earthcomputer.multiconnect.ConnectionInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

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

    public static final int DEFAULT_ABSENT_ID = -1;

    private static Map<Class<? extends Entity>, List<TrackedData<?>>> DEFAULT_DATA = new HashMap<>();
    private static Map<Class<? extends Entity>, Integer> NEXT_IDS = new HashMap<>();
    private static boolean dirty = false;

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
            setId(data, DEFAULT_ABSENT_ID);
        }
    }

    public static void onConnectToServer() {
        reregisterAll();
    }

    public static void reregisterAll() {
        NEXT_IDS.clear();
        Set<Class<? extends Entity>> alreadyReregistered = new HashSet<>();
        for (Class<? extends Entity> clazz : DEFAULT_DATA.keySet()) {
            reregisterDataForClass(clazz, alreadyReregistered);
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
    }

}
