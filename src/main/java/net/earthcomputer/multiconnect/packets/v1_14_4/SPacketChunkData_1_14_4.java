package net.earthcomputer.multiconnect.packets.v1_14_4;

import net.earthcomputer.multiconnect.ap.Datafix;
import net.earthcomputer.multiconnect.ap.DatafixTypes;
import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.ChunkData;
import net.earthcomputer.multiconnect.packets.SPacketChunkData;
import net.minecraft.nbt.NbtCompound;

import java.util.List;

@MessageVariant(maxVersion = Protocols.V1_14_4)
public class SPacketChunkData_1_14_4 implements SPacketChunkData {
    @Type(Types.INT)
    public int x;
    @Type(Types.INT)
    public int z;
    public boolean fullChunk;
    public int verticalStripBitmask;
    public NbtCompound heightmaps;
    @Length(raw = true)
    public ChunkData data;
    @Datafix(DatafixTypes.BLOCK_ENTITY)
    public List<NbtCompound> blockEntities;
}
