package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class CPacketJigsawGenerating {
    public CommonTypes.BlockPos pos;
    public int levels;
    public boolean keepJigsaws;
}
