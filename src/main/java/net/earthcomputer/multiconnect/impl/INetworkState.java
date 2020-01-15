package net.earthcomputer.multiconnect.impl;

import net.minecraft.network.NetworkSide;
import net.minecraft.network.Packet;

import java.util.Map;

public interface INetworkState {

    Map<NetworkSide, ? extends IPacketHandler<?>> getPacketHandlers();

    void multiconnect_onAddPacket(Class<? extends Packet<?>> packet);

}
