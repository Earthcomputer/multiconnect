package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class SPacketEntityStatus {
    public int entityId;
    public byte status;
}
