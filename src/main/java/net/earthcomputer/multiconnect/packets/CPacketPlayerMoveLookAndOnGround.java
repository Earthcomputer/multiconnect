package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class CPacketPlayerMoveLookAndOnGround {
    public float yaw;
    public float pitch;
    public boolean onGround;
}
