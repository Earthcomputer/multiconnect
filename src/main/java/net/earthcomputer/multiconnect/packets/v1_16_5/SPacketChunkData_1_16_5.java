package net.earthcomputer.multiconnect.packets.v1_16_5;

import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.ap.Datafix;
import net.earthcomputer.multiconnect.ap.DatafixTypes;
import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.OnlyIf;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.minecraft.nbt.NbtCompound;

import java.util.List;

@Message
public class SPacketChunkData_1_16_5 {
    @Type(Types.INT)
    public int x;
    @Type(Types.INT)
    public int z;
    public boolean fullChunk;
    public int verticalStripBitmask;
    public NbtCompound heightmaps;
    @OnlyIf(field = "fullChunk", condition = "hasFullChunk")
    public IntList biomes;
    public byte[] data;
    @Datafix(DatafixTypes.BLOCK_ENTITY)
    public List<NbtCompound> blockEntities;

    public static boolean hasFullChunk(boolean fullChunk) {
        return fullChunk;
    }
}
