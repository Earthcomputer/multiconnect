package net.earthcomputer.multiconnect.impl;

import net.minecraft.network.Packet;

import java.util.function.Supplier;

public final class PacketInfo<T extends Packet<?>> {

    private final Class<T> packetClass;
    private final Supplier<T> factory;

    private PacketInfo(Class<T> packetClass, Supplier<T> factory) {
        this.packetClass = packetClass;
        this.factory = factory;
    }

    public static <T extends Packet<?>> PacketInfo of(Class<T> packetClass, Supplier<T> factory) {
        return new PacketInfo<>(packetClass, factory);
    }

    public Class<T> getPacketClass() {
        return packetClass;
    }

    public Supplier<T> getFactory() {
        return factory;
    }

    @Override
    public int hashCode() {
        return packetClass.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (other.getClass() != PacketInfo.class) return false;
        PacketInfo that = (PacketInfo) other;
        return this.packetClass == that.packetClass;
    }

    @Override
    public String toString() {
        return packetClass.toString();
    }
}
