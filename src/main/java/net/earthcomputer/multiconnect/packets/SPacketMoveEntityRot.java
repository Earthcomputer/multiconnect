package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class SPacketMoveEntityRot {
    public int entityId;
    public byte yaw;
    public byte pitch;
    public boolean onGround;
}
