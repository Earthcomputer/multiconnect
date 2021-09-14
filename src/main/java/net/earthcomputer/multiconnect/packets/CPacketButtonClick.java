package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class CPacketButtonClick {
    public byte syncId;
    public byte buttonId;
}
