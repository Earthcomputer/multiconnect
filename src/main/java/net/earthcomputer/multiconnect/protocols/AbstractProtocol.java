package net.earthcomputer.multiconnect.protocols;

import com.google.common.collect.BiMap;
import net.earthcomputer.multiconnect.impl.DataTrackerManager;
import net.earthcomputer.multiconnect.impl.INetworkState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.Packet;

import java.util.*;

public abstract class AbstractProtocol {

    public void setup() {
        modifyPacketLists();
        DataTrackerManager.onConnectToServer();
    }

    protected void modifyPacketLists() {
        ((INetworkState) NetworkState.PLAY).getPacketHandlerMap().clear();

        for (Class<? extends Packet<?>> packet : getClientboundPackets()) {
            ((INetworkState) NetworkState.PLAY).multiconnect_addPacket(NetworkSide.CLIENTBOUND, packet);
        }
        for (Class<? extends Packet<?>> packet : getServerboundPackets()) {
            ((INetworkState) NetworkState.PLAY).multiconnect_addPacket(NetworkSide.SERVERBOUND, packet);
        }
    }

    public List<Class<? extends Packet<?>>> getClientboundPackets() {
        return new ArrayList<>(DefaultPackets.CLIENTBOUND);
    }

    public List<Class<? extends Packet<?>>> getServerboundPackets() {
        return new ArrayList<>(DefaultPackets.SERVERBOUND);
    }

    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        return true;
    }

    static {
        DefaultPackets.initialize();
    }

    private static class DefaultPackets {
        private static List<Class<? extends Packet<?>>> CLIENTBOUND = new ArrayList<>();
        private static List<Class<? extends Packet<?>>> SERVERBOUND = new ArrayList<>();

        private static void initialize() {
            Map<NetworkSide, BiMap<Integer, Class<? extends Packet<?>>>> packetHandlerMap = ((INetworkState) NetworkState.PLAY).getPacketHandlerMap();
            BiMap<Integer, Class<? extends Packet<?>>> clientPacketMap = packetHandlerMap.get(NetworkSide.CLIENTBOUND);
            CLIENTBOUND.addAll(clientPacketMap.values());
            CLIENTBOUND.sort(Comparator.comparing(packet -> clientPacketMap.inverse().get(packet)));
            BiMap<Integer, Class<? extends Packet<?>>> serverPacketMap = packetHandlerMap.get(NetworkSide.SERVERBOUND);
            SERVERBOUND.addAll(serverPacketMap.values());
            SERVERBOUND.sort(Comparator.comparing(packet -> serverPacketMap.inverse().get(packet)));
        }
    }

}
