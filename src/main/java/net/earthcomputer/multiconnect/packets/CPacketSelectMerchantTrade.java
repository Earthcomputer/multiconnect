package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.v1_12_2.CPacketCustomPayload_1_12_2;

@MessageVariant(minVersion = Protocols.V1_13)
public class CPacketSelectMerchantTrade {
    public int slot;

    @Handler(protocol = Protocols.V1_12_2)
    public static CPacketCustomPayload_1_12_2 toCustomPayload(
            @Argument("slot") int slot
    ) {
        var packet = new CPacketCustomPayload_1_12_2.TradeSelect();
        packet.channel = "MC|TrSel";
        packet.slot = slot;
        return packet;
    }
}
