package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketUpdateAttributes;

import java.util.List;

@MessageVariant(maxVersion = Protocols.V1_16_5)
public class SPacketUpdateAttributes_1_16_5 implements SPacketUpdateAttributes {
    public int entityId;
    @Length(type = Types.INT)
    public List<SPacketUpdateAttributes.Property> properties;
}
