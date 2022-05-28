package net.earthcomputer.multiconnect.packets.v1_13_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketSynchronizeTags;
import net.earthcomputer.multiconnect.packets.latest.SPacketSynchronizeTags_Latest;

import java.util.List;

@MessageVariant(maxVersion = Protocols.V1_13_2)
public class SPacketSynchronizeTags_1_13_2 implements SPacketSynchronizeTags {
    public List<SPacketSynchronizeTags_Latest.Tag> blocks;
    public List<SPacketSynchronizeTags_Latest.Tag> items;
    public List<SPacketSynchronizeTags_Latest.Tag> fluids;
}
