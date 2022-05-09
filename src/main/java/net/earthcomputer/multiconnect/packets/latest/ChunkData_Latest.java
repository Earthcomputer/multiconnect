package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.GlobalData;
import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.ChunkData;
import net.earthcomputer.multiconnect.protocols.generic.DimensionTypeReference;

import java.util.List;

@MessageVariant(minVersion = Protocols.V1_18)
public class ChunkData_Latest implements ChunkData {
    @Length(compute = "computeSectionsLength")
    public List<ChunkData.Section> sections;

    public static int computeSectionsLength(
            @GlobalData DimensionTypeReference dimensionType
    ) {
        return dimensionType.value().value().getHeight() >> 4;
    }

    @MessageVariant(minVersion = Protocols.V1_18)
    public static class ChunkSection implements ChunkData.Section {
        public short nonEmptyBlockCount;
        public ChunkData.BlockStatePalettedContainer blockStates;
        public BiomePalettedContainer biomes;
    }

    @MessageVariant(minVersion = Protocols.V1_18)
    @Polymorphic
    public static abstract class BlockStatePalettedContainer implements ChunkData.BlockStatePalettedContainer {
        public byte paletteSize;

        @MessageVariant
        @Polymorphic(intValue = 0)
        public static class Singleton extends BlockStatePalettedContainer {
            @Registry(Registries.BLOCK_STATE)
            public int blockStateId;
            @Type(Types.LONG)
            public long[] dummyData;
        }

        @MessageVariant
        @Polymorphic(intValue = {1, 2, 3, 4, 5, 6, 7, 8})
        public static class Multiple extends BlockStatePalettedContainer {
            @Registry(Registries.BLOCK_STATE)
            public int[] palette;
            @Type(Types.LONG)
            public long[] data;
        }

        @MessageVariant
        @Polymorphic(otherwise = true)
        public static class RegistryContainer extends BlockStatePalettedContainer {
            @Type(Types.LONG)
            public long[] data;
        }
    }

    @MessageVariant
    @Polymorphic
    public static abstract class BiomePalettedContainer {
        public byte paletteSize;

        @MessageVariant
        @Polymorphic(intValue = 0)
        public static class Singleton extends BiomePalettedContainer {
            public int biomeId;
            @Type(Types.LONG)
            public long[] dummyData;
        }

        @MessageVariant
        @Polymorphic(intValue = {1, 2, 3})
        public static class Multiple extends BiomePalettedContainer {
            public int[] palette;
            @Type(Types.LONG)
            public long[] data;
        }

        @MessageVariant
        @Polymorphic(otherwise = true)
        public static class RegistryContainer extends BiomePalettedContainer {
            @Type(Types.LONG)
            public long[] data;
        }
    }
}
