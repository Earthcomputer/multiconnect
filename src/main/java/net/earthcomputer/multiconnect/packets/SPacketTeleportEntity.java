package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class SPacketTeleportEntity {
    public int entityId;
    public double x;
    public double y;
    public double z;
    public byte yaw;
    public byte pitch;
    public boolean onGround;
}
