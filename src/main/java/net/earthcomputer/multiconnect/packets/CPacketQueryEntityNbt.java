package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class CPacketQueryEntityNbt {
    public int transactionId;
    public int entityId;
}
