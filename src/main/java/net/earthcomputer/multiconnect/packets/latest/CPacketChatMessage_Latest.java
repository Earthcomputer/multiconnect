package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketChatMessage;

@MessageVariant(minVersion = Protocols.V1_19)
public class CPacketChatMessage_Latest implements CPacketChatMessage {
    public String message;
    @Type(Types.LONG)
    public long timestamp;
    @Type(Types.LONG)
    public long salt;
    public byte[] signature;
    public boolean signedPreview;
}
