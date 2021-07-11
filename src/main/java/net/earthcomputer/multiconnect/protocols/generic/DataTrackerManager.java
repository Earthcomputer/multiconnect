package net.earthcomputer.multiconnect.protocols.generic;

import io.netty.handler.codec.DecoderException;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.mixin.bridge.DataTrackerAccessor;
import net.earthcomputer.multiconnect.mixin.bridge.EntityAccessor;
import net.earthcomputer.multiconnect.mixin.bridge.TrackedDataAccessor;
import net.earthcomputer.multiconnect.protocols.v1_8.Protocol_1_8;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.network.PacketByteBuf;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * This class is used to add and remove {@linkplain TrackedData} to entity types.
 * <p>
 * <strong>How to use:</strong><br/>
 *
 * If a TrackedData was introduced in a version, then the protocol before that version needs to remove that tracked data
 * again to be able to connect to prior versions. Likewise, if a TrackedData was removed in a version, the protocol
 * before that version needs to re-add that tracked data and decide how to handle it (if at all) when it's received.
 * <p>
 * To remove a TrackedData, override the acceptEntityData method in the protocol class, check that the class is equal to
 * the class that the TrackedData was declared in, and that the given TrackedData is equal to the TrackedData you want
 * to remove (you may need to use an accessor mixin), and return false if they match. At the end of the method, return
 * super.acceptEntityData.
 * <p>
 * To add a TrackedData, you first need to create it. Add a static final field for it in the protocol class and create
 * it with {@linkplain DataTrackerManager#createOldTrackedData(TrackedDataHandler)}, passing in the data type. Then,
 * find the TrackedData that you want to insert this TrackedData before (make sure it's in the same class). If you are
 * <i>not</i> removing the TrackedData that you're inserting before in this protocol (i.e. not replacing it), then you
 * need to override the preAcceptEntityData method in the protocol class, making sure to call super.preAcceptEntityData
 * at the end of the method. Inside that method, check against the class and the TrackedData that you're inserting
 * before, then call {@linkplain DataTrackerManager#registerOldTrackedData(Class, TrackedData, Object, BiConsumer)} with
 * the class, the TrackedData you want to insert, its default value and the handler for when it's received. The reason
 * to use this method is because an older protocol could remove the TrackedData you're anchoring against, meaning your
 * code doesn't run; preAcceptEntityData always happens before that. If you <i>are</i> removing the TrackedData you're
 * anchoring against in the same protocol, you can instead register
 * the old TrackedData om tje acceptEntityData before the return false.
 * <p>
 * If you're adding your TrackedData at the end of the list of TrackedData's for an entity class, there is nothing to
 * insert it before. In this case, you can override postEntityDataRegister in the protocol class, check against the
 * entity class and register the TrackedData in there, which adds it to the end of the list for that class. Call
 * super.postEntityDataRegister at the end of the method.
 */
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

    private static final Map<Class<? extends Entity>, List<TrackedData<?>>> DEFAULT_DATA = new HashMap<>();
    private static final Map<Class<? extends Entity>, Integer> NEXT_IDS = new HashMap<>();
    private static final Map<Class<? extends Entity>, List<Pair<TrackedData<?>, ?>>> oldTrackedData = new HashMap<>();
    private static final Map<TrackedData<?>, BiConsumer<?, ?>> oldTrackedDataHandlers = new IdentityHashMap<>();
    private static volatile boolean dirty = false;
    private static int nextAbsentId = -1;
    private static final Set<DataTracker> trackerInstances = Collections.newSetFromMap(new WeakHashMap<>());

    public static synchronized void addTrackerInstance(DataTracker instance) {
        trackerInstances.add(instance);
    }

    public static <T> TrackedData<T> createOldTrackedData(TrackedDataHandler<T> type) {
        return type.create(-1);
    }

    /**
     * Called from inside a protocol's preAcceptEntityData, acceptEntityData or postEntityDataRegister method to
     * register an old tracked data no longer in the current version. Will insert the new data before the current data
     * being checked.
     */
    public static <T, U extends Entity> void registerOldTrackedData(Class<U> clazz, TrackedData<T> data, T _default, BiConsumer<? super U, ? super T> handler) {
        ConnectionInfo.protocol.preAcceptEntityData(clazz, data);
        if (ConnectionInfo.protocol.acceptEntityData(clazz, data)) {
            int id = getNextId(clazz);
            NEXT_IDS.put(clazz, id + 1);
            ((TrackedDataAccessor) data).setId(id);
        } else {
            ((TrackedDataAccessor) data).setId(nextAbsentId--);
        }
        oldTrackedData.computeIfAbsent(clazz, k -> new ArrayList<>()).add(Pair.of(data, _default));
        oldTrackedDataHandlers.put(data, handler);
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
            // must reregister now in case entity needs tracked data from postEntityDataRegister when that entity otherwise doesn't have any data
            // otherwise we could just set dirty = true
            reregisterAll();
        }
    }

    public static synchronized void onCreateDataEntry() {
        if (dirty)
            reregisterAll();
    }

    private static void reregisterData(Class<? extends Entity> clazz, TrackedData<?> data) {
        ConnectionInfo.protocol.preAcceptEntityData(clazz, data);
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
        var alreadyReregistered = new HashSet<Class<? extends Entity>>();
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
            var trackedData = oldTrackedData.get(clazz);
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

    public static List<DataTracker.Entry<?>> deserializePacket(PacketByteBuf buf) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            return Protocol_1_8.deserializeDataTrackerEntries(buf);
        } else if (ConnectionInfo.protocolVersion <= Protocols.V1_9) {
            ArrayList<DataTracker.Entry<?>> entries = null;

            short id;
            while ((id = buf.readUnsignedByte()) != 255) {
                if (entries == null) {
                    entries = new ArrayList<>();
                }

                int serializerId = buf.readUnsignedByte();
                TrackedDataHandler<?> serializer = TrackedDataHandlerRegistry.get(serializerId);
                if (serializer == null) {
                    throw new DecoderException("Unknown serializer type " + serializerId);
                }

                entries.add(DataTrackerAccessor.callEntryFromPacket(buf, id, serializer));
            }

            return entries;
        } else {
            return DataTracker.deserializePacket(buf);
        }
    }

}
