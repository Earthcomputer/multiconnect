package net.earthcomputer.multiconnect.impl;

import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;

import java.util.List;
import java.util.function.Supplier;

public interface IPacketHandler<T extends INetHandler> {

    void multiconnect_clear();

    <P extends IPacket<T>> IPacketHandler<T> multiconnect_register(Class<P> clazz, Supplier<P> factory);

    Class<? extends IPacket<T>> multiconnect_getPacketClassById(int id);

    List<PacketInfo<? extends IPacket<T>>> multiconnect_values();

}
