package net.earthcomputer.multiconnect.packets.v1_14_4;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketRespawn;

@MessageVariant(minVersion = Protocols.V1_14, maxVersion = Protocols.V1_14_4)
public class SPacketRespawn_1_14_4 implements SPacketRespawn {
    @Type(Types.INT)
    public int dimensionId;
    @Type(Types.UNSIGNED_BYTE)
    public int gamemode;
    public String genType;
}
