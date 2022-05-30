package net.earthcomputer.multiconnect.packets.v1_13_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.GlobalData;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.OnlyIf;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.ChunkData;
import net.earthcomputer.multiconnect.packets.v1_12_2.ChunkData_1_12_2;
import net.earthcomputer.multiconnect.packets.v1_17_1.ChunkData_1_17_1;
import net.earthcomputer.multiconnect.protocols.generic.DimensionTypeReference;

@MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_13_2)
public class ChunkSection_1_13_2 implements ChunkData.Section {
    @Introduce(compute = "computeBlockStates")
    public ChunkData.BlockStatePalettedContainer blockStates;
    @Length(constant = 16 * 16 * 16 / 2)
    public byte[] blockLight;
    @Length(constant = 16 * 16 * 16 / 2)
    @OnlyIf("hasSkyLight")
    public byte[] skyLight;

    public static ChunkData.BlockStatePalettedContainer computeBlockStates(
            @Argument("blockStates") ChunkData.BlockStatePalettedContainer blockStates_
    ) {
        var blockStates = (ChunkData_1_12_2.BlockStatePalettedContainer) blockStates_;
        if (blockStates.paletteSize > 8) {
            var newBlockStates = new ChunkData_1_17_1.BlockStatePalettedContainer.RegistryContainer();
            newBlockStates.paletteSize = blockStates.paletteSize;
            newBlockStates.data = blockStates.data;
            return newBlockStates;
        } else {
            var newBlockStates = new ChunkData_1_17_1.BlockStatePalettedContainer.Multiple();
            newBlockStates.paletteSize = blockStates.paletteSize;
            newBlockStates.palette = blockStates.palette;
            newBlockStates.data = blockStates.data;
            return newBlockStates;
        }
    }

    public static boolean hasSkyLight(
            @GlobalData DimensionTypeReference dimType
    ) {
        return dimType.value().value().hasSkyLight();
    }
}
