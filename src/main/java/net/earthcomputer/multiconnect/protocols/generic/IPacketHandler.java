package net.earthcomputer.multiconnect.protocols.generic;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.PacketListener;

import java.util.List;
import java.util.function.Function;

public interface IPacketHandler<T extends PacketListener> {

    void multiconnect_clear();

    <P extends Packet<T>> IPacketHandler<T> multiconnect_register(Class<P> clazz, Function<PacketByteBuf, P> factory);

    <P extends Packet<T>> PacketInfo<? extends Packet<T>> multiconnect_getPacketInfoById(int id);

    List<PacketInfo<? extends Packet<T>>> multiconnect_values();

}
