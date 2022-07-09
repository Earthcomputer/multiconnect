package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class CPacketMoveVehicle {
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;
}
