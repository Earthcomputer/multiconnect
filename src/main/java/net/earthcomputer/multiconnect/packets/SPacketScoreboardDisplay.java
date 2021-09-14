package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class SPacketScoreboardDisplay {
    public byte position;
    public String scoreName;
}
