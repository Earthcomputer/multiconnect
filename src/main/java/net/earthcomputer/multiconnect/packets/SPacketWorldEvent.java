package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;

@MessageVariant
public class SPacketWorldEvent {
    @Type(Types.INT)
    public int id;
    public CommonTypes.BlockPos location;
    @Type(Types.INT)
    public int data;
    public boolean global;
}
