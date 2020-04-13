package net.earthcomputer.multiconnect.protocols.v1_13_2;

import net.minecraft.network.play.server.SChunkDataPacket;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

public class PendingChunkDataPackets {

    private static boolean processingQueuedPackets = false;
    private static final Deque<SChunkDataPacket> deque = new ArrayDeque<>();

    public static void push(SChunkDataPacket packet) {
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

    public static void processPackets(Consumer<SChunkDataPacket> handler) {
        processingQueuedPackets = true;
        try {
            deque.forEach(handler);
        } finally {
            processingQueuedPackets = false;
            deque.clear();
        }
    }

}
