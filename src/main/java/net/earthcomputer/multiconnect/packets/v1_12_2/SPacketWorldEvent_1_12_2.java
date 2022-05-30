package net.earthcomputer.multiconnect.packets.v1_12_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketWorldEvent;

@MessageVariant(maxVersion = Protocols.V1_12_2)
public class SPacketWorldEvent_1_12_2 implements SPacketWorldEvent {
    @Type(Types.INT)
    public int id;
    public CommonTypes.BlockPos location;
    @Type(Types.INT)
    public int data;
    public boolean global;
}
