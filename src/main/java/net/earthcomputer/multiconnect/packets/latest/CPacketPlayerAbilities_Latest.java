package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketPlayerAbilities;

@MessageVariant(minVersion = Protocols.V1_16)
public class CPacketPlayerAbilities_Latest implements CPacketPlayerAbilities {
    public byte flags;
}
