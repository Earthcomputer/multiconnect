package net.earthcomputer.multiconnect.protocols.generic;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;

import java.util.function.Function;

public final class PacketInfo<T extends Packet<?>> {

    private final Class<T> packetClass;
    private final Function<PacketByteBuf, T> factory;

    private PacketInfo(Class<T> packetClass, Function<PacketByteBuf, T> factory) {
        this.packetClass = packetClass;
        this.factory = factory;
    }

    public static <T extends Packet<?>> PacketInfo<T> of(Class<T> packetClass, Function<PacketByteBuf, T> factory) {
        return new PacketInfo<>(packetClass, factory);
    }

    public Class<T> getPacketClass() {
        return packetClass;
    }

    public Function<PacketByteBuf, T> getFactory() {
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
        PacketInfo<?> that = (PacketInfo<?>) other;
        return this.packetClass == that.packetClass;
    }

    @Override
    public String toString() {
        return packetClass.toString();
    }
}
