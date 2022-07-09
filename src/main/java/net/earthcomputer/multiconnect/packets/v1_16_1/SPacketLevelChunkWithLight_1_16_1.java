package net.earthcomputer.multiconnect.packets.v1_16_1;

import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Datafix;
import net.earthcomputer.multiconnect.ap.DatafixTypes;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.OnlyIf;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.ChunkData;
import net.earthcomputer.multiconnect.packets.SPacketLevelChunkWithLight;
import net.earthcomputer.multiconnect.protocols.v1_16.Protocol_1_16_5;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.fixes.BitStorageAlignFix;
import java.util.List;

@MessageVariant(minVersion = Protocols.V1_16, maxVersion = Protocols.V1_16_1)
public class SPacketLevelChunkWithLight_1_16_1 implements SPacketLevelChunkWithLight {
    @Type(Types.INT)
    public int x;
    @Type(Types.INT)
    public int z;
    public boolean fullChunk;
    @Introduce(booleanValue = true)
    public boolean forgetOldData;
    public int verticalStripBitmask;
    @Introduce(compute = "computeHeightmaps")
    public CompoundTag heightmaps;
    @Length(constant = Protocol_1_16_5.BIOME_ARRAY_LENGTH)
    @Type(Types.INT)
    @OnlyIf("hasFullChunk")
    public IntList biomes;
    @Length(raw = true)
    public ChunkData data;
    @Datafix(DatafixTypes.BLOCK_ENTITY)
    public List<CompoundTag> blockEntities;

    public static boolean hasFullChunk(@Argument("fullChunk") boolean fullChunk) {
        return fullChunk;
    }

    public static CompoundTag computeHeightmaps(@Argument("heightmaps") CompoundTag heightmaps) {
        if (heightmaps == null) {
            return null;
        }

        for (String key : heightmaps.getAllKeys()) {
            Tag nbt = heightmaps.get(key);
            if (nbt instanceof LongArrayTag nbtLongArray) {
                heightmaps.putLongArray(key, BitStorageAlignFix.addPadding(256, 9, nbtLongArray.getAsLongArray()));
            }
        }

        return heightmaps;
    }
}
