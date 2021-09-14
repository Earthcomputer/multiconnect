package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class CPacketPlayerMoveLookAndOnGround {
    public float yaw;
    public float pitch;
    public boolean onGround;
}
