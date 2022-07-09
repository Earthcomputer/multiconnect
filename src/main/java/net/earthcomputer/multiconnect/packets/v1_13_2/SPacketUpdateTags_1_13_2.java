package net.earthcomputer.multiconnect.packets.v1_13_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketUpdateTags;
import net.earthcomputer.multiconnect.packets.latest.SPacketUpdateTags_Latest;

import java.util.List;

@MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_13_2)
public class SPacketUpdateTags_1_13_2 implements SPacketUpdateTags {
    public List<SPacketUpdateTags_Latest.Tag> blocks;
    public List<SPacketUpdateTags_Latest.Tag> items;
    public List<SPacketUpdateTags_Latest.Tag> fluids;
}
