package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketKeepAlive;

@MessageVariant(minVersion = Protocols.V1_12_2)
public class CPacketKeepAlive_Latest implements CPacketKeepAlive {
    @Type(Types.LONG)
    public long id;
}
