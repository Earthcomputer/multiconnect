package net.earthcomputer.multiconnect.packets.v1_12_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketMapUpdate;

@MessageVariant(maxVersion = Protocols.V1_12_2)
public class MapIcon_1_12_2 implements SPacketMapUpdate.Icon {
    public byte metadata;
    public byte x;
    public byte z;
}
