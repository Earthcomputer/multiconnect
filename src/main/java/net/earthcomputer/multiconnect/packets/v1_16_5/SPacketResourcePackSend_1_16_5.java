package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketResourcePackSend;

@MessageVariant(maxVersion = Protocols.V1_16_5)
public class SPacketResourcePackSend_1_16_5 implements SPacketResourcePackSend {
    public String url;
    public String hash;
}
