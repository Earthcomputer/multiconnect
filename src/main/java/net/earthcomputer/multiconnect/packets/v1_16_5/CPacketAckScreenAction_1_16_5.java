package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;

@MessageVariant
public class CPacketAckScreenAction_1_16_5 {
    @Type(Types.UNSIGNED_BYTE)
    public int syncId;
    public short actionId;
    public boolean accepted;
}
