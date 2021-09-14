package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;

@Message
public class SPacketScreenHandlerPropertyUpdate {
    @Type(Types.UNSIGNED_BYTE)
    public int syncId;
    public short property;
    public short value;
}
