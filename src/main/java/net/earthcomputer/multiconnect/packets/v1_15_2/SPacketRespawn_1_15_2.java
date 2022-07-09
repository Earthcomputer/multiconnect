package net.earthcomputer.multiconnect.packets.v1_15_2;

import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketRespawn;

@MessageVariant(minVersion = Protocols.V1_15, maxVersion = Protocols.V1_15_2)
public class SPacketRespawn_1_15_2 implements SPacketRespawn {
    @Type(Types.INT)
    public int dimensionId;
    @Type(Types.LONG)
    @Introduce(intValue = 0)
    public long hashedSeed;
    @Type(Types.UNSIGNED_BYTE)
    public int gamemode;
    public String genType;
}
