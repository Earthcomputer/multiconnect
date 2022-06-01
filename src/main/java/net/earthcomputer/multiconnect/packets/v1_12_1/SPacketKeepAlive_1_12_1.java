package net.earthcomputer.multiconnect.packets.v1_12_1;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketKeepAlive;

@MessageVariant(maxVersion = Protocols.V1_12_1)
public class SPacketKeepAlive_1_12_1 implements SPacketKeepAlive {
    public int id;
}
