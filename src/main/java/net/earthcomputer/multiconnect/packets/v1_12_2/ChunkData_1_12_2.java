package net.earthcomputer.multiconnect.packets.v1_12_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.GlobalData;
import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.OnlyIf;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.ChunkData;
import net.earthcomputer.multiconnect.protocols.generic.DimensionTypeReference;
import net.minecraft.core.RegistryAccess;
import java.util.List;

@MessageVariant(maxVersion = Protocols.V1_12_2)
public class ChunkData_1_12_2 implements ChunkData {
    @Length(compute = "computeSectionsLength")
    public List<net.earthcomputer.multiconnect.packets.v1_12_2.ChunkData_1_12_2.Section> sections;
    @Type(Types.UNSIGNED_BYTE)
    @Length(constant = 256)
    @OnlyIf("isFullChunk")
    public int[] biomes;

    public static int computeSectionsLength(
            @Argument("outer.verticalStripBitmask") int verticalStripBitmask
    ) {
        return Integer.bitCount(verticalStripBitmask & 0xffff);
    }

    public static boolean isFullChunk(@Argument("outer.fullChunk") boolean fullChunk) {
        return fullChunk;
    }

    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class Section implements ChunkData.Section {
        public ChunkData.BlockStatePalettedContainer blockStates;
        @Length(constant = 16 * 16 * 16 / 2)
        public byte[] blockLight;
        @Length(constant = 16 * 16 * 16 / 2)
        @OnlyIf("hasSkyLight")
        public byte[] skyLight;

        public static boolean hasSkyLight(
                @GlobalData RegistryAccess registryManager,
                @GlobalData DimensionTypeReference dimType
        ) {
            return dimType.getValue(registryManager).hasSkyLight();
        }
    }

    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class BlockStatePalettedContainer implements ChunkData.BlockStatePalettedContainer {
        public byte paletteSize;
        public int[] palette;
        @Type(Types.LONG)
        public long[] data;
    }
}
