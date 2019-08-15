package net.earthcomputer.multiconnect.protocols.v1_14_3;

import net.earthcomputer.multiconnect.protocols.v1_14_4.Protocol_1_14_4;
import net.minecraft.network.Packet;

import java.util.List;

public class Protocol_1_14_3 extends Protocol_1_14_4 {

    @Override
    public List<Class<? extends Packet<?>>> getClientboundPackets() {
        List<Class<? extends Packet<?>>> packets = super.getClientboundPackets();
        packets.remove(packets.size() - 1); // BlockPlayerActionS2CPacket
        return packets;
    }

}
