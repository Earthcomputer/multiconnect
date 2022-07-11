package net.earthcomputer.multiconnect.packets.v1_14_4;

import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketLogin;

@MessageVariant(minVersion = Protocols.V1_14, maxVersion = Protocols.V1_14_4)
public class SPacketLogin_1_14_4 implements SPacketLogin {
    @Type(Types.INT)
    public int entityId;
    @Type(Types.UNSIGNED_BYTE)
    public int gamemode;
    @Type(Types.INT)
    public int dimensionId;
    @Type(Types.UNSIGNED_BYTE)
    public int maxPlayers;
    public String genType;
    @Introduce(intValue = 64)
    public int viewDistance;
    public boolean reducedDebugInfo;
}
