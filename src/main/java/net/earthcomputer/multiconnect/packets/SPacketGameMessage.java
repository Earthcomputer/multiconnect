package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

import java.util.UUID;

@MessageVariant
public class SPacketGameMessage {
    public CommonTypes.Text text;
    public byte position;
    public UUID sender;
}
