package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketSynchronizeTags;

import java.util.List;

@Message(variantOf = SPacketSynchronizeTags.class, maxVersion = Protocols.V1_16_5)
public class SPacketSynchronizeTags_1_16_5 {
    public List<SPacketSynchronizeTags.Group> groups;
}
