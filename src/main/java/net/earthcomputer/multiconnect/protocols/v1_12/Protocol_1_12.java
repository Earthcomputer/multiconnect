package net.earthcomputer.multiconnect.protocols.v1_12;

import net.earthcomputer.multiconnect.impl.PacketInfo;
import net.earthcomputer.multiconnect.protocols.v1_12_1.Protocol_1_12_1;
import net.minecraft.network.play.client.CConfirmTeleportPacket;
import net.minecraft.network.play.client.CPlaceRecipePacket;
import net.minecraft.network.play.server.SPlaceGhostRecipePacket;

import java.util.List;

public class Protocol_1_12 extends Protocol_1_12_1 {

    @Override
    public List<PacketInfo<?>> getClientboundPackets() {
        List<PacketInfo<?>> packets = super.getClientboundPackets();
        remove(packets, SPlaceGhostRecipePacket.class);
        return packets;
    }

    @Override
    public List<PacketInfo<?>> getServerboundPackets() {
        List<PacketInfo<?>> packets = super.getServerboundPackets();
        remove(packets, CPlaceRecipePacket.class);
        insertAfter(packets, CConfirmTeleportPacket.class, PacketInfo.of(PlaceRecipeC2SPacket_1_12.class, PlaceRecipeC2SPacket_1_12::new));
        return packets;
    }
}
