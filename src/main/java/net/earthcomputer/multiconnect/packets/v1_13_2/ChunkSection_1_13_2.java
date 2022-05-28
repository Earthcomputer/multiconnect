package net.earthcomputer.multiconnect.packets.v1_13_2;

import net.earthcomputer.multiconnect.ap.GlobalData;
import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.OnlyIf;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.ChunkData;
import net.earthcomputer.multiconnect.protocols.generic.DimensionTypeReference;

@MessageVariant(maxVersion = Protocols.V1_13_2)
public class ChunkSection_1_13_2 implements ChunkData.Section {
    public ChunkData.BlockStatePalettedContainer blockStates;
    @Length(constant = 16 * 16 * 16 / 2)
    public byte[] blockLight;
    @Length(constant = 16 * 16 * 16 / 2)
    @OnlyIf("hasSkyLight")
    public byte[] skyLight;

    public static boolean hasSkyLight(
            @GlobalData DimensionTypeReference dimType
    ) {
        return dimType.value().value().hasSkyLight();
    }
}
