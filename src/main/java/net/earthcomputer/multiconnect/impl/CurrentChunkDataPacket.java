package net.earthcomputer.multiconnect.impl;

import net.minecraft.network.play.server.SChunkDataPacket;

import java.util.ArrayDeque;
import java.util.Deque;

public class CurrentChunkDataPacket {

    private static Deque<SChunkDataPacket> packets = new ArrayDeque<>();

    public static SChunkDataPacket get() {
        return packets.peek();
    }

    public static void push(SChunkDataPacket packet) {
        packets.push(packet);
    }

    public static void pop() {
        packets.pop();
    }

}
