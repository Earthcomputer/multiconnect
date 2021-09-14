package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class CPacketVehicleMove {
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;
}
