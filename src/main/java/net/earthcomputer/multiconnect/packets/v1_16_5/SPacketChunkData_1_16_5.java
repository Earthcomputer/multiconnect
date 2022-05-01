package net.earthcomputer.multiconnect.packets.v1_16_5;

import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Datafix;
import net.earthcomputer.multiconnect.ap.DatafixTypes;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.OnlyIf;
import net.earthcomputer.multiconnect.ap.PartialHandler;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.ChunkData;
import net.earthcomputer.multiconnect.packets.SPacketChunkData;
import net.earthcomputer.multiconnect.protocols.generic.TypedMap;
import net.earthcomputer.multiconnect.protocols.v1_16_5.Protocol_1_16_5;
import net.minecraft.nbt.NbtCompound;

import java.util.List;

@MessageVariant(maxVersion = Protocols.V1_16_5)
public class SPacketChunkData_1_16_5 implements SPacketChunkData {
    @Type(Types.INT)
    public int x;
    @Type(Types.INT)
    public int z;
    public boolean fullChunk;
    public int verticalStripBitmask;
    public NbtCompound heightmaps;
    @OnlyIf("hasFullChunk")
    public IntList biomes;
    @Length(raw = true)
    public ChunkData data;
    @Datafix(DatafixTypes.BLOCK_ENTITY)
    public List<NbtCompound> blockEntities;

    public static boolean hasFullChunk(@Argument("fullChunk") boolean fullChunk) {
        return fullChunk;
    }

    @PartialHandler
    public static void saveFullChunk(
            @Argument("fullChunk") boolean fullChunk,
            @FilledArgument TypedMap userData
    ) {
        userData.put(Protocol_1_16_5.FULL_CHUNK_KEY, fullChunk);
    }
}
