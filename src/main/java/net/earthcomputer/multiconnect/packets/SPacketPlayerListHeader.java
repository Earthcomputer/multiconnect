package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class SPacketPlayerListHeader {
    public CommonTypes.Text header;
    public CommonTypes.Text footer;
}
