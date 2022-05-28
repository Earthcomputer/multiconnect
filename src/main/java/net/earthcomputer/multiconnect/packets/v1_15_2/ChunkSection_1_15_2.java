package net.earthcomputer.multiconnect.packets.v1_15_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.packets.ChunkData;

@MessageVariant(minVersion = Protocols.V1_14, maxVersion = Protocols.V1_15_2)
public class ChunkSection_1_15_2 implements ChunkData.Section {
    @Introduce(compute = "computeNonEmptyBlockCount")
    public short nonEmptyBlockCount;
    public ChunkData.BlockStatePalettedContainer blockStates;

    public static short computeNonEmptyBlockCount(
            @Argument("blockStates") ChunkData.BlockStatePalettedContainer blockStates_,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.BLOCK_STATE, value = "air")) int airId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.BLOCK_STATE, value = "void_air")) int voidAirId,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.BLOCK_STATE, value = "cave_air")) int caveAirId
    ) {
        var blockStates = (BlockStatePalettedContainer) blockStates_;
        boolean isRegistryPalette = blockStates.paletteSize > 8;
        int count = 0;
        for (int i = 0; i < 4096; i++) {
            int stateId = Utils.getOldPackedBitArrayElement(blockStates.data, i, blockStates.paletteSize);
            if (!isRegistryPalette) {
                stateId = blockStates.palette[stateId];
            }
            if (stateId != airId && stateId != voidAirId && stateId != caveAirId) {
                count++;
            }
        }
        return (short) count;
    }

    @MessageVariant(maxVersion = Protocols.V1_15_2)
    public static class BlockStatePalettedContainer implements ChunkData.BlockStatePalettedContainer {
        public byte paletteSize;
        @Registry(Registries.BLOCK_STATE)
        public int[] palette;
        @Type(Types.LONG)
        public long[] data;
    }
}
