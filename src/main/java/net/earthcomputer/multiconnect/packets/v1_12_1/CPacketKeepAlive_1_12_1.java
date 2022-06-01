package net.earthcomputer.multiconnect.packets.v1_12_1;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketKeepAlive;

@MessageVariant(maxVersion = Protocols.V1_12_1)
public class CPacketKeepAlive_1_12_1 implements CPacketKeepAlive {
    public int id;
}
