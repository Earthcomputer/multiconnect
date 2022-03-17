package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class CPacketJigsawGenerating {
    public CommonTypes.BlockPos pos;
    public int levels;
    public boolean keepJigsaws;
}
