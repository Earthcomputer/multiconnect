package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketUpdateTags;
import net.earthcomputer.multiconnect.packets.latest.SPacketUpdateTags_Latest;

import java.util.List;

@MessageVariant(minVersion = Protocols.V1_14, maxVersion = Protocols.V1_16_5)
public class SPacketUpdateTags_1_16_5 implements SPacketUpdateTags {
    public List<SPacketUpdateTags_Latest.Tag> blocks;
    public List<SPacketUpdateTags_Latest.Tag> items;
    public List<SPacketUpdateTags_Latest.Tag> fluids;
    @Introduce(defaultConstruct = true)
    public List<SPacketUpdateTags_Latest.Tag> entities;
}
