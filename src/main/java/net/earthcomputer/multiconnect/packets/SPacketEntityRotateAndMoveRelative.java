package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class SPacketEntityRotateAndMoveRelative {
    public int entityId;
    public short deltaX;
    public short deltaY;
    public short deltaZ;
    public byte yaw;
    public byte pitch;
    public boolean onGround;
}
