package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class SPacketEntityVelocityUpdate {
    public int entityId;
    public short velocityX;
    public short velocityY;
    public short velocityZ;
}
