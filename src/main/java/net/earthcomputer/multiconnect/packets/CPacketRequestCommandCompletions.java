package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class CPacketRequestCommandCompletions {
    public int transactionId;
    public String text;
}
