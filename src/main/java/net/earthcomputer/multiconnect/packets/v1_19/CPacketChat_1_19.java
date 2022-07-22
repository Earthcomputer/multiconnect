package net.earthcomputer.multiconnect.packets.v1_19;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketChat;

@MessageVariant(minVersion = Protocols.V1_19, maxVersion = Protocols.V1_19)
public class CPacketChat_1_19 implements CPacketChat {
    public String message;
    @Type(Types.LONG)
    public long timestamp;
    @Type(Types.LONG)
    public long salt;
    public byte[] signature;
    public boolean signedPreview;
}
