package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class CPacketPlayerInteractBlock {
    public CommonTypes.Hand hand;
    public CommonTypes.BlockPos pos;
    public CommonTypes.Direction face;
    public float offsetX;
    public float offsetY;
    public float offsetZ;
    public boolean insideBlock;
}
