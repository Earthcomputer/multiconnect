package net.earthcomputer.multiconnect.protocols.v1_12;

import net.earthcomputer.multiconnect.protocols.generic.PacketInfo;
import net.earthcomputer.multiconnect.protocols.v1_12_1.Protocol_1_12_1;
import net.minecraft.network.packet.c2s.play.CraftRequestC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.CraftFailedResponseS2CPacket;

import java.util.List;

public class Protocol_1_12 extends Protocol_1_12_1 {

    @Override
    public List<PacketInfo<?>> getClientboundPackets() {
        List<PacketInfo<?>> packets = super.getClientboundPackets();
        remove(packets, CraftFailedResponseS2CPacket.class);
        return packets;
    }

    @Override
    public List<PacketInfo<?>> getServerboundPackets() {
        List<PacketInfo<?>> packets = super.getServerboundPackets();
        remove(packets, CraftRequestC2SPacket.class);
        insertAfter(packets, TeleportConfirmC2SPacket.class, PacketInfo.of(PlaceRecipeC2SPacket_1_12.class, PlaceRecipeC2SPacket_1_12::new));
        return packets;
    }
}
