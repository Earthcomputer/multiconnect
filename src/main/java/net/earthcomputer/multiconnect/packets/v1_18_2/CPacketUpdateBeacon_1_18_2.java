package net.earthcomputer.multiconnect.packets.v1_18_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketUpdateBeacon;
import net.earthcomputer.multiconnect.packets.v1_12_2.CPacketCustomPayload_1_12_2;

import java.util.OptionalInt;

@MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_18_2)
public class CPacketUpdateBeacon_1_18_2 implements CPacketUpdateBeacon {
    @Registry(Registries.STATUS_EFFECT)
    @Introduce(compute = "computePrimaryEffect")
    public int primaryEffect;
    @Registry(Registries.STATUS_EFFECT)
    @Introduce(compute = "computeSecondaryEffect")
    public int secondaryEffect;

    public static int computePrimaryEffect(@Argument("primaryEffect") OptionalInt primaryEffect) {
        return primaryEffect.orElse(-1);
    }

    public static int computeSecondaryEffect(@Argument("secondaryEffect") OptionalInt secondaryEffect) {
        return secondaryEffect.orElse(-1);
    }

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
