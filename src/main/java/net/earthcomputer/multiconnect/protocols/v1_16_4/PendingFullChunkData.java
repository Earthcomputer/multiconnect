package net.earthcomputer.multiconnect.protocols.v1_16_4;

import net.minecraft.util.math.ChunkPos;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class PendingFullChunkData {
    private static final long CACHE_TIME = 10000000000L;
    private static final Map<ChunkPos, Entry> isFullChunks = new LinkedHashMap<>();

    public static void setPendingFullChunk(ChunkPos pos, boolean isFullChunk) {
        long now = System.nanoTime();
        synchronized (isFullChunks) {
            Iterator<Entry> itr = isFullChunks.values().iterator();
            while (itr.hasNext()) {
                Entry entry = itr.next();
                if (now - entry.timestamp > CACHE_TIME) {
                    itr.remove();
                } else {
                    break;
                }
            }
            isFullChunks.put(pos, new Entry(now, isFullChunk));
        }
    }

    public static boolean isFullChunk(ChunkPos pos) {
        Entry entry;
        synchronized (isFullChunks) {
            entry = isFullChunks.remove(pos);
        }
        return entry == null || entry.isFullChunk;
    }

    private static class Entry {
        private final long timestamp;
        private final boolean isFullChunk;

        private Entry(long timestamp, boolean isFullChunk) {
            this.timestamp = timestamp;
            this.isFullChunk = isFullChunk;
        }
    }
}
