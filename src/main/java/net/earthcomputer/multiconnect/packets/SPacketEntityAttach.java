package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;

@Message
public class SPacketEntityAttach {
    @Type(Types.INT)
    public int attached;
    @Type(Types.INT)
    public int holding;
}
