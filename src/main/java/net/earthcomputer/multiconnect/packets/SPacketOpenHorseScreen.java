package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;

@Message
public class SPacketOpenHorseScreen {
    @Type(Types.UNSIGNED_BYTE)
    public byte syncId;
    public int slotCount;
    @Type(Types.INT)
    public int entityId;
}
