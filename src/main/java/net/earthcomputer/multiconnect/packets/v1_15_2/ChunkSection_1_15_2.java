package net.earthcomputer.multiconnect.packets.v1_15_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.ChunkData;

@MessageVariant(maxVersion = Protocols.V1_15_2)
public class ChunkSection_1_15_2 implements ChunkData.Section {
    public short nonEmptyBlockCount;
    public ChunkData.BlockStatePalettedContainer blockStates;

    @MessageVariant(maxVersion = Protocols.V1_15_2)
    public static class BlockStatePalettedContainer implements ChunkData.BlockStatePalettedContainer {
        public byte paletteSize;
        @Registry(Registries.BLOCK_STATE)
        public int[] palette;
        @Type(Types.LONG)
        public long[] data;
    }
}
