package net.earthcomputer.multiconnect.protocols.v1_13_2;

import net.minecraft.util.math.ChunkPos;

import java.util.HashMap;
import java.util.Map;

public class PendingLightData {

    private static final Map<ChunkPos, PendingLightData> instances = new HashMap<>();

    public static void setInstance(int chunkX, int chunkZ, PendingLightData instance) {
        synchronized (instances) {
            if (instance == null)
                instances.remove(new ChunkPos(chunkX, chunkZ));
            else
                instances.put(new ChunkPos(chunkX, chunkZ), instance);
        }
    }

    public static PendingLightData getInstance(int chunkX, int chunkZ) {
        synchronized (instances) {
            return instances.get(new ChunkPos(chunkX, chunkZ));
        }
    }

    private byte[][] blockLight = new byte[16][];
    private byte[][] skyLight = new byte[16][];

    public void setBlockLight(int sectionY, byte[] blockLight) {
        this.blockLight[sectionY] = blockLight;
    }

    public void setSkyLight(int sectionY, byte[] skyLight) {
        this.skyLight[sectionY] = skyLight;
    }

    public byte[] getBlockLight(int sectionY) {
        return blockLight[sectionY];
    }

    public byte[] getSkyLight(int sectionY) {
        return skyLight[sectionY];
    }

}
