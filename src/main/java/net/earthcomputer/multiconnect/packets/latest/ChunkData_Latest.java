package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.ChunkData;

import java.util.BitSet;
import java.util.List;

@MessageVariant(minVersion = Protocols.V1_17)
public class ChunkData_Latest implements ChunkData {
    @Length(compute = "computeSectionsLength")
    public List<ChunkSection> sections;

    public static int computeSectionsLength(
        @Argument("outer.verticalStripBitmask") BitSet verticalStripBitmask
    ) {
        return verticalStripBitmask.cardinality();
    }

    @MessageVariant
    public static class ChunkSection {
        public short nonEmptyBlockCount;
        public Palette palette;
        @Type(Types.LONG)
        public long[] data;
    }

    @MessageVariant
    public static class Palette {
        public byte paletteSize;
        @Registry(Registries.BLOCK_STATE)
        public int[] palette;
    }
}
