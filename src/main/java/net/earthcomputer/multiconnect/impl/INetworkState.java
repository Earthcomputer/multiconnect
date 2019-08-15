package net.earthcomputer.multiconnect.impl;

import com.google.common.collect.BiMap;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.Packet;

import java.util.Map;

public interface INetworkState {

    Map<NetworkSide, BiMap<Integer, Class<? extends Packet<?>>>> getPacketHandlerMap();

    void multiconnect_addPacket(NetworkSide side, Class<? extends Packet<?>> packet);

}
