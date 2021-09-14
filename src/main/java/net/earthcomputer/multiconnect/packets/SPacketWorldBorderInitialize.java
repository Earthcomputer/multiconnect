package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class SPacketWorldBorderInitialize {
    public double x;
    public double z;
    public double size;
    public double sizeLerpTarget;
    public long sizeLerpTime;
    public int maxRadius;
    public int warningBlocks;
    public int warningTime;
}
