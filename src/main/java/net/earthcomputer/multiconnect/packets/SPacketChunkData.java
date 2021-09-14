package net.earthcomputer.multiconnect.packets;

import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.ap.DatafixTypes;
import net.earthcomputer.multiconnect.ap.Datafix;
import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.minecraft.nbt.NbtCompound;

import java.util.BitSet;
import java.util.List;

@Message
public class SPacketChunkData {
    @Type(Types.INT)
    public int x;
    @Type(Types.INT)
    public int z;
    public BitSet verticalStripBitmask;
    public NbtCompound heightmaps;
    public IntList biomes;
    public byte[] data;
    @Datafix(DatafixTypes.BLOCK_ENTITY)
    public List<NbtCompound> blockEntities;
}
