package net.earthcomputer.multiconnect.protocols.v1_14_4;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;

import java.util.HashMap;
import java.util.Map;

public class PendingBiomeData {

    private static final Map<ChunkPos, Biome[]> pendingBiomes = new HashMap<>();

    public static Biome[] getPendingBiomeData(int chunkX, int chunkZ) {
        synchronized (pendingBiomes) {
            return pendingBiomes.get(new ChunkPos(chunkX, chunkZ));
        }
    }

    public static void setPendingBiomeData(int chunkX, int chunkZ, Biome[] data) {
        synchronized (pendingBiomes) {
            if (data == null)
                pendingBiomes.remove(new ChunkPos(chunkX, chunkZ));
            else
                pendingBiomes.put(new ChunkPos(chunkX, chunkZ), data);
        }
    }

}
