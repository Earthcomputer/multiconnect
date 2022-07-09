package net.earthcomputer.multiconnect.packets.v1_15_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketUpdateAttributes;
import net.earthcomputer.multiconnect.packets.latest.SPacketUpdateAttributes_Latest;

import java.util.List;

@MessageVariant(maxVersion = Protocols.V1_15_2)
public class SPacketEntityAttributesProperty_1_15_2 implements SPacketUpdateAttributes.Property {
    public String key;
    public double value;
    public List<SPacketUpdateAttributes_Latest.Property.Modifier> modifiers;
}
