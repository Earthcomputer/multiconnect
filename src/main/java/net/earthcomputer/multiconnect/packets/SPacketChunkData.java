package net.earthcomputer.multiconnect.packets;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.DatafixTypes;
import net.earthcomputer.multiconnect.ap.Datafix;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.protocols.v1_16_5.Protocol_1_16_5;
import net.minecraft.nbt.NbtCompound;

import java.util.BitSet;
import java.util.List;

@Message(minVersion = Protocols.V1_17)
public class SPacketChunkData {
    @Type(Types.INT)
    public int x;
    @Type(Types.INT)
    public int z;
    @Introduce(compute = "computeVerticalStripBitmask")
    public BitSet verticalStripBitmask;
    public NbtCompound heightmaps;
    @Introduce(compute = "computeBiomes")
    public IntList biomes;
    public byte[] data;
    @Datafix(DatafixTypes.BLOCK_ENTITY)
    public List<NbtCompound> blockEntities;

    public static BitSet computeVerticalStripBitmask(@Argument("verticalStripBitmask") int verticalStripBitmask) {
        return BitSet.valueOf(new long[] {verticalStripBitmask});
    }

    public static IntList computeBiomes(
            @Argument("fullChunk") boolean fullChunk,
            @Argument("biomes") IntList biomes
    ) {
        if (fullChunk) {
            return biomes;
        } else {
            // TODO: get the actual biome array from somewhere
            return new IntArrayList(new int[Protocol_1_16_5.BIOME_ARRAY_LENGTH]);
        }
    }
}
