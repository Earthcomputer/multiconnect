package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class CPacketPlayerMovePositionAndOnGround {
    public double x;
    public double y;
    public double z;
    public boolean onGround;
}
