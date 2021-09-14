package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class SPacketPlayerSpawnPosition {
    public CommonTypes.BlockPos pos;
    public byte angle;
}
