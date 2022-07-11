package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketBlockChangedAck;

@MessageVariant(minVersion = Protocols.V1_19)
public class SPacketBlockChangedAck_Latest implements SPacketBlockChangedAck {
    public int sequence;
}
