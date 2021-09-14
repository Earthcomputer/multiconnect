package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;

@Message
public class SPacketBlockUpdate {
    public CommonTypes.BlockPos pos;
    @Registry(Registries.BLOCK_STATE)
    public int stateId;
}
