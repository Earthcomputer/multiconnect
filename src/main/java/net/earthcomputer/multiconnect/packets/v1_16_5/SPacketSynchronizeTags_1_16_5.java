package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketSynchronizeTags;
import net.earthcomputer.multiconnect.packets.latest.SPacketSynchronizeTags_Latest;

import java.util.List;

@MessageVariant(maxVersion = Protocols.V1_16_5)
public class SPacketSynchronizeTags_1_16_5 implements SPacketSynchronizeTags {
    public List<SPacketSynchronizeTags_Latest.BlockGroup.Tag> blocks;
    public List<SPacketSynchronizeTags_Latest.ItemGroup.Tag> items;
    public List<SPacketSynchronizeTags_Latest.FluidGroup.Tag> fluids;
    public List<SPacketSynchronizeTags_Latest.EntityTypeGroup.Tag> entities;
}
