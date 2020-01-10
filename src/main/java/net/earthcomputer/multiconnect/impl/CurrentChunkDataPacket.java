package net.earthcomputer.multiconnect.impl;

import net.minecraft.client.network.packet.ChunkDataS2CPacket;

import java.util.ArrayDeque;
import java.util.Deque;

public class CurrentChunkDataPacket {

    private static Deque<ChunkDataS2CPacket> packets = new ArrayDeque<>();

    public static ChunkDataS2CPacket get() {
        return packets.peek();
    }

    public static void push(ChunkDataS2CPacket packet) {
        packets.push(packet);
    }

    public static void pop() {
        packets.pop();
    }

}
