package net.earthcomputer.multiconnect.protocols.generic;

import io.netty.handler.codec.DecoderException;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.mixin.bridge.DataTrackerAccessor;
import net.earthcomputer.multiconnect.mixin.bridge.EntityAccessor;
import net.earthcomputer.multiconnect.mixin.bridge.TrackedDataAccessor;
import net.earthcomputer.multiconnect.protocols.v1_8.Protocol_1_8;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * This class is used to add and remove {@linkplain EntityDataAccessor} to entity types.
 * <p>
 * <strong>How to use:</strong><br/>
 *
 * If a SynchedEntityData was introduced in a version, then the protocol before that version needs to remove that data
 * again to be able to connect to prior versions. Likewise, if a SynchedEntityData was removed in a version, the
 * protocol before that version needs to re-add that data and decide how to handle it (if at all) when it's received.
 * <p>
 * To remove a SynchedEntityData, override the acceptEntityData method in the protocol class, check that the class is
 * equal to the class that the SynchedEntityData was declared in, and that the given SynchedEntityData is equal to the
 * SynchedEntityData you want to remove (you may need to use an accessor mixin), and return false if they match. At the
 * end of the method, return super.acceptEntityData.
 * <p>
 * To add a SynchedEntityData, you first need to create it. Add a static final field for it in the protocol class and
 * create it with {@linkplain SynchedDataManager#createOldEntityData(EntityDataSerializer)}, passing in the data type.
 * Then, find the SynchedEntityData that you want to insert this SynchedEntityData before (make sure it's in the same
 * class). If you are <i>not</i> removing the SynchedEntityData that you're inserting before in this protocol (i.e. not
 * replacing it), then you need to override the preAcceptEntityData method in the protocol class, making sure to call
 * super.preAcceptEntityData at the end of the method. Inside that method, check against the class and the
 * SynchedEntityData that you're inserting before, then call
 * {@linkplain SynchedDataManager#registerOldEntityData(Class, EntityDataAccessor, Object, BiConsumer)} with the class,
 * the SynchedEntityData you want to insert, its default value and the handler for when it's received. The reason to use
 * this method is because an older protocol could remove the SynchedEntityData you're anchoring against, meaning your
 * code doesn't run; preAcceptEntityData always happens before that. If you <i>are</i> removing the SynchedEntityData
 * you're anchoring against in the same protocol, you can instead register the old SynchedEntityData from the
 * acceptEntityData before the return false.
 * <p>
 * If you're adding your SynchedEntityData at the end of the list of SynchedEntityData's for an entity class, there is
 * nothing to insert it before. In this case, you can override postEntityDataRegister in the protocol class, check
 * against the entity class and register the SynchedEntityData in there, which adds it to the end of the list for that
 * class. Call super.postEntityDataRegister at the end of the method.
 */
public class SynchedDataManager {
    /*
     * We can't get default data upfront, because that requires the entity class list.
     * Instead, we save the order when vanilla registers the data via SynchedEntityData.defineId.
     * We cannot rewrite the data IDs right at this point, as AbstractProtocol subclasses use
     * the static fields in the entity classes to identify data, but they will still be null
     * because the data has yet to be returned by registerData.
     * Therefore, when defineId is called, an ID redefinition is scheduled by
     * setting the "dirty" flag to true. The redefinition happens at the first opportunity, before
     * the first time a data item is added to an entity instance.
     * Redefinitions go through all known entity classes so far which we've collected via our
     * hook in defineId and rewrites the IDs based on the current protocol's
     * acceptEntityData method.
     * Redefinitions also happen when connecting to a new server.
     */

    private static final Map<Class<? extends Entity>, List<EntityDataAccessor<?>>> DEFAULT_DATA = new HashMap<>();
    private static final Map<Class<? extends Entity>, Integer> NEXT_IDS = new HashMap<>();
    private static final Map<Class<? extends Entity>, List<Pair<EntityDataAccessor<?>, ?>>> oldData = new HashMap<>();
    private static final Map<EntityDataAccessor<?>, BiConsumer<?, ?>> oldDataHandlers = new IdentityHashMap<>();
    private static volatile boolean dirty = false;
    private static int nextAbsentId = -1;
    private static final Set<SynchedEntityData> trackerInstances = Collections.newSetFromMap(new WeakHashMap<>());

    public static synchronized void addTrackerInstance(SynchedEntityData instance) {
        trackerInstances.add(instance);
    }

    public static <T> EntityDataAccessor<T> createOldEntityData(EntityDataSerializer<T> type) {
        return type.createAccessor(-1);
    }

    /**
     * Called from inside a protocol's preAcceptEntityData, acceptEntityData or postEntityDataRegister method to
     * register an old tracked data no longer in the current version. Will insert the new data before the current data
     * being checked.
     */
    public static <T, U extends Entity> void registerOldEntityData(Class<U> clazz, EntityDataAccessor<T> data, T _default, BiConsumer<? super U, ? super T> handler) {
        ConnectionInfo.protocol.preAcceptEntityData(clazz, data);
        if (ConnectionInfo.protocol.acceptEntityData(clazz, data)) {
            int id = getNextId(clazz);
            NEXT_IDS.put(clazz, id + 1);
            ((TrackedDataAccessor) data).setId(id);
        } else {
            ((TrackedDataAccessor) data).setId(nextAbsentId--);
        }
        oldData.computeIfAbsent(clazz, k -> new ArrayList<>()).add(Pair.of(data, _default));
        oldDataHandlers.put(data, handler);
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

    public static synchronized void onDefineId(Class<? extends Entity> clazz, EntityDataAccessor<?> data) {
        DEFAULT_DATA.computeIfAbsent(clazz, k -> new ArrayList<>()).add(data);
        dirty = true;
    }

    public static synchronized void postRegisterData(Class<? extends Entity> clazz) {
        if (!DEFAULT_DATA.containsKey(clazz)) {
            DEFAULT_DATA.put(clazz, Collections.emptyList());
            // must reregister now in case entity needs tracked data from postEntityDataRegister when that entity otherwise doesn't have any data
            // otherwise we could just set dirty = true
            redefineAllIds();
        }
    }

    public static synchronized void onDefineDataItem() {
        if (dirty) {
            redefineAllIds();
        }
    }

    private static void redefineIds(Class<? extends Entity> clazz, EntityDataAccessor<?> data) {
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
        redefineAllIds();
    }

    private static void redefineAllIds() {
        nextAbsentId = -1;
        NEXT_IDS.clear();
        oldData.clear();
        oldDataHandlers.clear();
        var alreadyReregistered = new HashSet<Class<? extends Entity>>();
        for (Class<? extends Entity> clazz : DEFAULT_DATA.keySet()) {
            redefineIdsForClass(clazz, alreadyReregistered);
        }
        for (SynchedEntityData tracker : trackerInstances) {
            ((ISynchedEntityData) tracker).multiconnect_recomputeItems();
        }
        dirty = false;
    }

    @SuppressWarnings("unchecked")
    private static void redefineIdsForClass(Class<? extends Entity> clazz, Set<Class<? extends Entity>> alreadyReregistered) {
        if (!alreadyReregistered.add(clazz))
            return;

        if (clazz != Entity.class)
            redefineIdsForClass((Class<? extends Entity>) clazz.getSuperclass(), alreadyReregistered);

        if (DEFAULT_DATA.containsKey(clazz))
            for (EntityDataAccessor<?> data : DEFAULT_DATA.get(clazz))
                redefineIds(clazz, data);
        ConnectionInfo.protocol.postEntityDataRegister(clazz);
    }

    public static synchronized void defineIdsForOldData(Entity entity) {
        for (Class<?> clazz = entity.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
            var trackedData = oldData.get(clazz);
            if (trackedData != null) {
                for (Pair<EntityDataAccessor<?>, ?> pair : trackedData) {
                    doDefineIds(entity, pair.getLeft(), pair.getRight());
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void doDefineIds(Entity entity, EntityDataAccessor<T> data, Object _default) {
        entity.getEntityData().define(data, (T) _default);
    }

    public static synchronized void handleOldData(Entity entity, EntityDataAccessor<?> data) {
        data = ((ISynchedEntityData) entity.getEntityData()).multiconnect_getActualData(data);
        BiConsumer<?, ?> handler = oldDataHandlers.get(data);
        if (handler != null)
            doHandleData(handler, entity, entity.getEntityData().get(data));
    }

    @SuppressWarnings("unchecked")
    private static <T, U> void doHandleData(BiConsumer<U, T> handler, Entity entity, Object val) {
        handler.accept((U) entity, (T) val);
    }

    public static void transferEntityData(Entity oldEntity, Entity newEntity) {
        ((ISynchedEntityData) oldEntity.getEntityData()).multiconnect_setEntityTo(newEntity);
        ((EntityAccessor) newEntity).setEntityData(oldEntity.getEntityData());
    }

    public static List<SynchedEntityData.DataItem<?>> deserializePacket(FriendlyByteBuf buf) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            return Protocol_1_8.deserializeDataTrackerEntries(buf);
        } else if (ConnectionInfo.protocolVersion <= Protocols.V1_9) {
            ArrayList<SynchedEntityData.DataItem<?>> entries = null;

            short id;
            while ((id = buf.readUnsignedByte()) != 255) {
                if (entries == null) {
                    entries = new ArrayList<>();
                }

                int serializerId = buf.readUnsignedByte();
                EntityDataSerializer<?> serializer = EntityDataSerializers.getSerializer(serializerId);
                if (serializer == null) {
                    throw new DecoderException("Unknown serializer type " + serializerId);
                }

                entries.add(DataTrackerAccessor.callEntryFromPacket(buf, id, serializer));
            }

            return entries;
        } else {
            return SynchedEntityData.unpack(buf);
        }
    }

}
