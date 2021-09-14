package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;

@Message
public class SPacketUnloadChunk {
    @Type(Types.INT)
    public int x;
    @Type(Types.INT)
    public int z;
}
