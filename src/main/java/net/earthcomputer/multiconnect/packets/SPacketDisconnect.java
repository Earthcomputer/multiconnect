package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class SPacketDisconnect {
    public CommonTypes.Text reason;
}
