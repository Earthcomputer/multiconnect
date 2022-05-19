package net.earthcomputer.multiconnect.packets.v1_15_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketGameJoin;

@MessageVariant(maxVersion = Protocols.V1_15_2)
public class SPacketGameJoin_1_15_2 implements SPacketGameJoin {
    @Type(Types.INT)
    public int entityId;
    @Type(Types.UNSIGNED_BYTE)
    public int gamemode;
    @Type(Types.INT)
    public int dimensionId;
    @Type(Types.LONG)
    public long hashedSeed;
    @Type(Types.UNSIGNED_BYTE)
    public int maxPlayers;
    public String genType;
    public int viewDistance;
    public boolean reducedDebugInfo;
    public boolean enableRespawnScreen;
}
