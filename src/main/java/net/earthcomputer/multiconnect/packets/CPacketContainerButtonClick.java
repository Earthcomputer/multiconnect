package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class CPacketContainerButtonClick {
    public byte syncId;
    public byte buttonId;
}
