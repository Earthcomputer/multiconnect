package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.v1_12_2.CPacketCustomPayload_1_12_2;

@MessageVariant(minVersion = Protocols.V1_13)
public class CPacketUpdateBeacon {
    @Registry(Registries.STATUS_EFFECT)
    public int primaryEffect;
    @Registry(Registries.STATUS_EFFECT)
    public int secondaryEffect;

    @Handler(protocol = Protocols.V1_12_2)
    public static CPacketCustomPayload_1_12_2 toCustomPayload(
            @Argument("primaryEffect") int primaryEffect,
            @Argument("secondaryEffect") int secondaryEffect
    ) {
        var packet = new CPacketCustomPayload_1_12_2.Beacon();
        packet.channel = "MC|Beacon";
        packet.primaryEffect = primaryEffect;
        packet.secondaryEffect = secondaryEffect;
        return packet;
    }
}
