package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class CPacketMovePlayerPos {
    public double x;
    public double y;
    public double z;
    public boolean onGround;
}
