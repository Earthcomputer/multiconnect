package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class SPacketMoveEntityPos {
    public int entityId;
    public short deltaX;
    public short deltaY;
    public short deltaZ;
    public boolean onGround;
}
