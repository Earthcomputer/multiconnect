package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;

@MessageVariant
public class SPacketDifficulty {
    @Type(Types.UNSIGNED_BYTE)
    public int difficulty;
    public boolean locked;
}
