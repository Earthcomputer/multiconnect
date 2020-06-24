package net.earthcomputer.multiconnect.protocols.generic;

import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;

import java.util.List;
import java.util.function.Supplier;

public interface IPacketHandler<T extends PacketListener> {

    void multiconnect_clear();

    <P extends Packet<T>> IPacketHandler<T> multiconnect_register(Class<P> clazz, Supplier<P> factory);

    Class<? extends Packet<T>> multiconnect_getPacketClassById(int id);

    List<PacketInfo<? extends Packet<T>>> multiconnect_values();

}
