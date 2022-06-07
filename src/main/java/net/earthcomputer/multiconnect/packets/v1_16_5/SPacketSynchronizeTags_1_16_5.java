package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketSynchronizeTags;
import net.earthcomputer.multiconnect.packets.latest.SPacketSynchronizeTags_Latest;

import java.util.List;

@MessageVariant(minVersion = Protocols.V1_14, maxVersion = Protocols.V1_16_5)
public class SPacketSynchronizeTags_1_16_5 implements SPacketSynchronizeTags {
    public List<SPacketSynchronizeTags_Latest.Tag> blocks;
    public List<SPacketSynchronizeTags_Latest.Tag> items;
    public List<SPacketSynchronizeTags_Latest.Tag> fluids;
    @Introduce(defaultConstruct = true)
    public List<SPacketSynchronizeTags_Latest.Tag> entities;
}
