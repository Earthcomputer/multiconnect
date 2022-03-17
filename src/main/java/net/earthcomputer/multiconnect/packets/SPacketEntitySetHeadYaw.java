package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class SPacketEntitySetHeadYaw {
    public int entityId;
    public byte angle;
}
