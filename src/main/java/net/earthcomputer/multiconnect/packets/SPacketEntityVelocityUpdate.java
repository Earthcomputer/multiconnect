package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class SPacketEntityVelocityUpdate {
    public int entityId;
    public short velocityX;
    public short velocityY;
    public short velocityZ;
}
