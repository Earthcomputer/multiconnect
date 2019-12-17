package net.earthcomputer.multiconnect.protocols.v1_14_3;

import net.earthcomputer.multiconnect.impl.PacketInfo;
import net.earthcomputer.multiconnect.protocols.v1_14_4.Protocol_1_14_4;

import java.util.List;

public class Protocol_1_14_3 extends Protocol_1_14_4 {

    @Override
    public List<PacketInfo<?>> getClientboundPackets() {
        List<PacketInfo<?>> packets = super.getClientboundPackets();
        packets.remove(packets.size() - 1); // BlockPlayerActionS2CPacket
        return packets;
    }

}
