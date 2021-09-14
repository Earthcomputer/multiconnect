package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class SPacketEntityRotate {
    public int entityId;
    public byte yaw;
    public byte pitch;
    public boolean onGround;
}
