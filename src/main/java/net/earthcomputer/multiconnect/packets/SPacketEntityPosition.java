package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class SPacketEntityPosition {
    public int entityId;
    public double x;
    public double y;
    public double z;
    public byte yaw;
    public byte pitch;
    public boolean onGround;
}
