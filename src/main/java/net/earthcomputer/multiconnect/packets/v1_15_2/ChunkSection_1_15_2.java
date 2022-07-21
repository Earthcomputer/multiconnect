package net.earthcomputer.multiconnect.packets.v1_15_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.packets.ChunkData;
import net.earthcomputer.multiconnect.packets.v1_17_1.ChunkData_1_17_1;
import net.earthcomputer.multiconnect.packets.v1_17_1.ChunkData_1_17_1.BlockStatePalettedContainer;
import net.earthcomputer.multiconnect.packets.v1_17_1.ChunkData_1_17_1.BlockStatePalettedContainer.Multiple;
import net.earthcomputer.multiconnect.packets.v1_17_1.ChunkData_1_17_1.BlockStatePalettedContainer.RegistryContainer;

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
        var blockStates = (ChunkData_1_17_1.BlockStatePalettedContainer) blockStates_;
        var palettedStates = blockStates instanceof ChunkData_1_17_1.BlockStatePalettedContainer.Multiple ? (ChunkData_1_17_1.BlockStatePalettedContainer.Multiple) blockStates : null;
        var nonPalettedStates = palettedStates == null ? (ChunkData_1_17_1.BlockStatePalettedContainer.RegistryContainer) blockStates : null;
        int count = 0;
        for (int i = 0; i < 4096; i++) {
            int stateId = Utils.getOldPackedBitArrayElement(palettedStates == null ? nonPalettedStates.data : palettedStates.data, i, blockStates.paletteSize);
            if (palettedStates != null) {
                stateId = palettedStates.palette[stateId];
            }
            if (stateId != airId && stateId != voidAirId && stateId != caveAirId) {
                count++;
            }
        }
        return (short) count;
    }
}
