package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketPlayerActionResponse;

@MessageVariant(minVersion = Protocols.V1_19)
public class SPacketPlayerActionResponse_Latest implements SPacketPlayerActionResponse {
    public int sequence;
}
