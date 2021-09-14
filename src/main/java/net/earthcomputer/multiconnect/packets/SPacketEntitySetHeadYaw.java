package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class SPacketEntitySetHeadYaw {
    public int entityId;
    public byte angle;
}
