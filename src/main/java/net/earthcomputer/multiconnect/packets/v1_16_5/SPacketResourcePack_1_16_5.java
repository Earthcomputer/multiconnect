package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketResourcePack;

@MessageVariant(maxVersion = Protocols.V1_16_5)
public class SPacketResourcePack_1_16_5 implements SPacketResourcePack {
    public String url;
    public String hash;
}
