package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class SPacketPlayerPositionLook_1_16_5 {
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;
    public byte flags;
    public int teleportId;
}
