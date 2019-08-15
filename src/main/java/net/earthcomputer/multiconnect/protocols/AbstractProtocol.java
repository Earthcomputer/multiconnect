package net.earthcomputer.multiconnect.protocols;

import com.google.common.collect.BiMap;
import net.earthcomputer.multiconnect.impl.INetworkState;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.Packet;

import java.util.*;

public abstract class AbstractProtocol {

    private static List<Class<? extends Packet<?>>> DEFAULT_CLIENTBOUND_PACKETS = new ArrayList<>();
    private static List<Class<? extends Packet<?>>> DEFAULT_SERVERBOUND_PACKETS = new ArrayList<>();
    static {
        Map<NetworkSide, BiMap<Integer, Class<? extends Packet<?>>>> packetHandlerMap = ((INetworkState) NetworkState.PLAY).getPacketHandlerMap();
        BiMap<Integer, Class<? extends Packet<?>>> clientPacketMap = packetHandlerMap.get(NetworkSide.CLIENTBOUND);
        DEFAULT_CLIENTBOUND_PACKETS.addAll(clientPacketMap.values());
        DEFAULT_CLIENTBOUND_PACKETS.sort(Comparator.comparing(packet -> clientPacketMap.inverse().get(packet)));
        BiMap<Integer, Class<? extends Packet<?>>> serverPacketMap = packetHandlerMap.get(NetworkSide.SERVERBOUND);
        DEFAULT_SERVERBOUND_PACKETS.addAll(serverPacketMap.values());
        DEFAULT_SERVERBOUND_PACKETS.sort(Comparator.comparing(packet -> serverPacketMap.inverse().get(packet)));
    }

    public void setup() {
        modifyProtocolLists();
    }

    protected void modifyProtocolLists() {
        ((INetworkState) NetworkState.PLAY).getPacketHandlerMap().clear();

        for (Class<? extends Packet<?>> packet : getClientboundPackets()) {
            ((INetworkState) NetworkState.PLAY).multiconnect_addPacket(NetworkSide.CLIENTBOUND, packet);
        }
        for (Class<? extends Packet<?>> packet : getServerboundPackets()) {
            ((INetworkState) NetworkState.PLAY).multiconnect_addPacket(NetworkSide.SERVERBOUND, packet);
        }
    }

    public List<Class<? extends Packet<?>>> getClientboundPackets() {
        return new ArrayList<>(DEFAULT_CLIENTBOUND_PACKETS);
    }

    public List<Class<? extends Packet<?>>> getServerboundPackets() {
        return new ArrayList<>(DEFAULT_SERVERBOUND_PACKETS);
    }

}
