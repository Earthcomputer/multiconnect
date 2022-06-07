package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.v1_12_2.CPacketCustomPayload_1_12_2;

@MessageVariant(minVersion = Protocols.V1_13)
public class CPacketRenameItem {
    public String name;

    @Handler(protocol = Protocols.V1_12_2)
    public static CPacketCustomPayload_1_12_2 toCustomPayload(
            @Argument("name") String name
    ) {
        var packet = new CPacketCustomPayload_1_12_2.ItemName();
        packet.channel = "MC|ItemName";
        packet.name = name;
        return packet;
    }
}
