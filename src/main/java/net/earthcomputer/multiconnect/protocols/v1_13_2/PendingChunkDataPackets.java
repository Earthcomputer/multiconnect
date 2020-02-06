package net.earthcomputer.multiconnect.protocols.v1_13_2;

import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

public class PendingChunkDataPackets {

    private static boolean processingQueuedPackets = false;
    private static final Deque<ChunkDataS2CPacket> deque = new ArrayDeque<>();

    public static void push(ChunkDataS2CPacket packet) {
        if (!processingQueuedPackets)
            deque.addLast(packet);
    }

    public static void pop() {
        if (!processingQueuedPackets)
            deque.removeLast();
    }

    public static boolean isProcessingQueuedPackets() {
        return processingQueuedPackets;
    }

    public static void processPackets(Consumer<ChunkDataS2CPacket> handler) {
        processingQueuedPackets = true;
        try {
            deque.forEach(handler);
        } finally {
            processingQueuedPackets = false;
            deque.clear();
        }
    }

}
