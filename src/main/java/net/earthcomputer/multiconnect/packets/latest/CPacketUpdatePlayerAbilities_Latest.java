package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketUpdatePlayerAbilities;

@MessageVariant(minVersion = Protocols.V1_16)
public class CPacketUpdatePlayerAbilities_Latest implements CPacketUpdatePlayerAbilities {
    public byte flags;
}
