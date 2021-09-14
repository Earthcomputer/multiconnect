package net.earthcomputer.multiconnect.packets;

import it.unimi.dsi.fastutil.longs.LongList;
import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;

@Message
public class SPacketChunkDeltaUpdate {
    @Type(Types.LONG)
    public long sectionPos;
    public boolean noLightUpdates;
    public LongList blocks;
}
