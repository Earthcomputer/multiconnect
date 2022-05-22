package net.earthcomputer.multiconnect.packets.v1_14_4;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.OnlyIf;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.ChunkData;

import java.util.List;

@MessageVariant(maxVersion = Protocols.V1_14_4)
public class ChunkData_1_14_4 implements ChunkData {
    @Length(compute = "computeSectionsLength")
    public List<Section> sections;
    @Type(Types.INT)
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
}
