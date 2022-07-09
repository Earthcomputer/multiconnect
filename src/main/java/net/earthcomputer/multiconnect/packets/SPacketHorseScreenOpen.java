package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;

@MessageVariant
public class SPacketHorseScreenOpen {
    @Type(Types.UNSIGNED_BYTE)
    public byte syncId;
    public int slotCount;
    @Type(Types.INT)
    public int entityId;
}
