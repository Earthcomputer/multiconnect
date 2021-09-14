package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;

@Message
public class SPacketDeathMessage {
    public int playerId;
    @Type(Types.INT)
    public int entityId;
    public CommonTypes.Text message;
}
