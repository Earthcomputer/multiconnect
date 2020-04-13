package net.earthcomputer.multiconnect.protocols.v1_14_4;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.datasync.EntityDataManager;

import java.util.List;

public class PendingDataTrackerEntries {

    private static final Int2ObjectMap<List<EntityDataManager.DataEntry<?>>> entries = new Int2ObjectOpenHashMap<>();

    public static List<EntityDataManager.DataEntry<?>> getRegistryObjects(int entityId) {
        synchronized (entries) {
            return entries.get(entityId);
        }
    }

    public static void setEntries(int entityId, List<EntityDataManager.DataEntry<?>> entries) {
        synchronized (PendingDataTrackerEntries.entries) {
            if (entries == null) {
                PendingDataTrackerEntries.entries.remove(entityId);
            } else {
                PendingDataTrackerEntries.entries.put(entityId, entries);
            }
        }
    }

}
