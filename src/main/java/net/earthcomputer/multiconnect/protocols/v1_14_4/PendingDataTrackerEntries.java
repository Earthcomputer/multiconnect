package net.earthcomputer.multiconnect.protocols.v1_14_4;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.data.DataTracker;

import java.util.List;

public class PendingDataTrackerEntries {

    private static final Int2ObjectMap<List<DataTracker.Entry<?>>> entries = new Int2ObjectOpenHashMap<>();

    public static List<DataTracker.Entry<?>> getEntries(int entityId) {
        synchronized (entries) {
            return entries.get(entityId);
        }
    }

    public static void setEntries(int entityId, List<DataTracker.Entry<?>> entries) {
        synchronized (PendingDataTrackerEntries.entries) {
            if (entries == null) {
                PendingDataTrackerEntries.entries.remove(entityId);
            } else {
                PendingDataTrackerEntries.entries.put(entityId, entries);
            }
        }
    }

}
