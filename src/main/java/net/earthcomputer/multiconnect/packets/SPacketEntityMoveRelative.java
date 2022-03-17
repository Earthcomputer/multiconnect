package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class SPacketEntityMoveRelative {
    public int entityId;
    public short deltaX;
    public short deltaY;
    public short deltaZ;
    public boolean onGround;
}
