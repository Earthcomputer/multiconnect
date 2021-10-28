package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketResourcePackSend;

@Message(variantOf = SPacketResourcePackSend.class, maxVersion = Protocols.V1_16_5)
public class SPacketResourcePackSend_1_16_5 {
    public String url;
    public String hash;
}
