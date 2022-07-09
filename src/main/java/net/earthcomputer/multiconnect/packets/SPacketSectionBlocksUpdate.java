package net.earthcomputer.multiconnect.packets;

import it.unimi.dsi.fastutil.longs.LongList;
import net.earthcomputer.multiconnect.ap.CustomFix;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.impl.PacketSystem;

@MessageVariant
public class SPacketSectionBlocksUpdate {
    @Type(Types.LONG)
    public long sectionPos;
    public boolean noLightUpdates;
    @CustomFix("fixBlocks")
    public LongList blocks;

    public static LongList fixBlocks(LongList blocks) {
        for (int i = 0; i < blocks.size(); i++) {
            long block = blocks.getLong(i);
            long newBlock = (block & 4095) | ((long) PacketSystem.serverBlockStateIdToClient((int) (block >>> 12)) << 12);
            blocks.set(i, newBlock);
        }
        return blocks;
    }
}
