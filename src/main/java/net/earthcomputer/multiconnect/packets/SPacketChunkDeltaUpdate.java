package net.earthcomputer.multiconnect.packets;

import it.unimi.dsi.fastutil.longs.LongList;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;

@MessageVariant
public class SPacketChunkDeltaUpdate {
    @Type(Types.LONG)
    public long sectionPos;
    public boolean noLightUpdates;
    public LongList blocks;
}
