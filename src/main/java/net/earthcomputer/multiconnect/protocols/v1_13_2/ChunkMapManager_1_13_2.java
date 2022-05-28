package net.earthcomputer.multiconnect.protocols.v1_13_2;

import net.earthcomputer.multiconnect.protocols.generic.Key;

import java.util.concurrent.atomic.AtomicInteger;

public record ChunkMapManager_1_13_2(
        boolean receivedPosLook,
        AtomicInteger minChunkX,
        AtomicInteger minChunkZ,
        AtomicInteger maxChunkX,
        AtomicInteger maxChunkZ,
        AtomicInteger currentMidX,
        AtomicInteger currentMidZ
) {
    public ChunkMapManager_1_13_2(boolean receivedPosLook) {
        this(receivedPosLook, new AtomicInteger(Integer.MAX_VALUE), new AtomicInteger(Integer.MAX_VALUE), new AtomicInteger(Integer.MIN_VALUE), new AtomicInteger(Integer.MIN_VALUE), new AtomicInteger(), new AtomicInteger());
    }

    public static final Key<Boolean> SYNTHETIC_POS_LOOK = Key.create("syntheticPosLook", false);
}
