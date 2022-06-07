package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class CPacketButtonClick {
    public byte syncId;
    public byte buttonId;
}
