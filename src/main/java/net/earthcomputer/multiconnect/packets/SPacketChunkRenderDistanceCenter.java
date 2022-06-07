package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Sendable;
import net.earthcomputer.multiconnect.api.Protocols;

@Sendable(from = Protocols.V1_14)
@MessageVariant
public class SPacketChunkRenderDistanceCenter {
    public int x;
    public int z;
}
