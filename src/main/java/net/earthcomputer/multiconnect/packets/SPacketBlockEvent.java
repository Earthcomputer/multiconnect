package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;

@Message
public class SPacketBlockEvent {
    public CommonTypes.BlockPos pos;
    @Type(Types.UNSIGNED_BYTE)
    public int type;
    @Type(Types.UNSIGNED_BYTE)
    public int data;
    @Registry(Registries.BLOCK)
    public int blockType;
}
