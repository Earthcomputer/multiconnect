package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

import java.util.UUID;

@Message
public class SPacketGameMessage {
    public CommonTypes.Text text;
    public byte position;
    public UUID sender;
}
