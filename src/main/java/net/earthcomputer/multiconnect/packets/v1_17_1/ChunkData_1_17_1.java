package net.earthcomputer.multiconnect.packets.v1_17_1;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.ChunkData;
import net.minecraft.datafixer.fix.BitStorageAlignFix;
import net.minecraft.util.math.MathHelper;

import java.util.BitSet;
import java.util.List;

@MessageVariant(minVersion = Protocols.V1_17, maxVersion = Protocols.V1_17_1)
public class ChunkData_1_17_1 implements ChunkData {
    @Length(compute = "computeSectionsLength")
    public List<ChunkData.Section> sections;

    public static int computeSectionsLength(
        @Argument("outer.verticalStripBitmask") BitSet verticalStripBitmask
    ) {
        return verticalStripBitmask.cardinality();
    }

    @MessageVariant(minVersion = Protocols.V1_16, maxVersion = Protocols.V1_17_1)
    public static class ChunkSection implements ChunkData.Section {
        public short nonEmptyBlockCount;
        @Introduce(compute = "translateBlockStates")
        public ChunkData.BlockStatePalettedContainer blockStates;

        public static ChunkData.BlockStatePalettedContainer translateBlockStates(
                @Argument("blockStates") ChunkData.BlockStatePalettedContainer blockStates_
        ) {
            var blockStates = (BlockStatePalettedContainer) blockStates_;
            if (blockStates.paletteSize != 0 && !MathHelper.isPowerOfTwo(blockStates.paletteSize)) {
                if (blockStates instanceof BlockStatePalettedContainer.Multiple multiple) {
                    multiple.data = BitStorageAlignFix.resizePackedIntArray(4096, blockStates.paletteSize, multiple.data);
                } else {
                    var registryContainer = (BlockStatePalettedContainer.RegistryContainer) blockStates;
                    registryContainer.data = BitStorageAlignFix.resizePackedIntArray(4096, blockStates.paletteSize, registryContainer.data);
                }
            }
            return blockStates;
        }
    }

    @Polymorphic
    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_17_1)
    public abstract static class BlockStatePalettedContainer implements ChunkData.BlockStatePalettedContainer {
        public byte paletteSize;

        @Polymorphic(intValue = {1, 2, 3, 4, 5, 6, 7, 8})
        @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_17_1)
        public static class Multiple extends BlockStatePalettedContainer {
            @Registry(Registries.BLOCK_STATE)
            public int[] palette;
            @Type(Types.LONG)
            public long[] data;
        }

        @Polymorphic(otherwise = true)
        @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_17_1)
        public static class RegistryContainer extends BlockStatePalettedContainer {
            @Type(Types.LONG)
            public long[] data;
        }
    }
}
