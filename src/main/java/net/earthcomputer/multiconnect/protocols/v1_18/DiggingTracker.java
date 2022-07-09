package net.earthcomputer.multiconnect.protocols.v1_18;

import net.earthcomputer.multiconnect.packets.v1_18_2.SPacketPlayerActionResponse_1_18_2;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public record DiggingTracker(
        ConcurrentMap<DiggingPos, Integer> pos2SequenceId,
        AtomicInteger maxSequenceId
) {
    public static DiggingTracker create() {
        return new DiggingTracker(new ConcurrentHashMap<>(), new AtomicInteger(-1));
    }

    public record DiggingPos(SPacketPlayerActionResponse_1_18_2.Action action, long pos) {
    }
}
