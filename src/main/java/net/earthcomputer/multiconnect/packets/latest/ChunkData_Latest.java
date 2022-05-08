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
        public Palette palette;
        @Type(Types.LONG)
        public long[] data;
        public BiomePalette biomePalette;
        @Type(Types.LONG)
        public long[] biomeData;
    }

    @MessageVariant
    public static class Palette {
        public byte paletteSize;
        @Registry(Registries.BLOCK_STATE)
        public int[] palette;
    }

    @MessageVariant
    @Polymorphic
    public static abstract class BiomePalette {
        public byte paletteSize;

        @MessageVariant
        @Polymorphic(intValue = 0)
        public static class Singleton extends BiomePalette {
            public int biomeId;
        }

        @MessageVariant
        @Polymorphic(intValue = {1, 2})
        public static class Multiple extends BiomePalette {
            public int[] palette;
        }

        @MessageVariant
        @Polymorphic(otherwise = true)
        public static class Registry extends BiomePalette {
        }
    }
}
