package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;

@Message
public class SPacketWorldEvent {
    @Type(Types.INT)
    public int id;
    public CommonTypes.BlockPos location;
    @Type(Types.INT)
    public int data;
    public boolean global;
}
