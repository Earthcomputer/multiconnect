package net.earthcomputer.multiconnect.impl;

import net.minecraft.network.IPacket;
import net.minecraft.network.PacketDirection;

import java.util.Map;

public interface IProtocolType {

    Map<PacketDirection, ? extends IPacketHandler<?>> getField_229711_h_();

    void multiconnect_onAddPacket(Class<? extends IPacket<?>> packet);

}
