package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class CPacketQueryEntityNbt {
    public int transactionId;
    public int entityId;
}
