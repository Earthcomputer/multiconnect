package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;

@MessageVariant
public class SPacketForgetLevelChunk {
    @Type(Types.INT)
    public int x;
    @Type(Types.INT)
    public int z;
}
