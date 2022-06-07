package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketLightUpdate;

import java.util.BitSet;

@MessageVariant(minVersion = Protocols.V1_17)
public class SPacketLightUpdate_Latest implements SPacketLightUpdate {
    public int chunkX;
    public int chunkZ;
    public boolean trustEdges;
    @Introduce(compute = "computeSkyLightMask")
    public BitSet skyLightMask;
    @Introduce(compute = "computeBlockLightMask")
    public BitSet blockLightMask;
    @Introduce(compute = "computeEmptySkyLightMask")
    public BitSet emptySkyLightMask;
    @Introduce(compute = "computeEmptyBlockLightMask")
    public BitSet emptyBlockLightMask;
    public byte[][] skyLightArrays;
    public byte[][] blockLightArrays;

    public static BitSet computeSkyLightMask(@Argument("skyLightMask") int skyLightMask) {
        return BitSet.valueOf(new long[] {skyLightMask & 0x3ffff});
    }

    public static BitSet computeBlockLightMask(@Argument("blockLightMask") int blockLightMask) {
        return BitSet.valueOf(new long[] {blockLightMask & 0x3ffff});
    }

    public static BitSet computeEmptySkyLightMask(@Argument("emptySkyLightMask") int emptySkyLightMask) {
        return BitSet.valueOf(new long[] {emptySkyLightMask & 0x3ffff});
    }

    public static BitSet computeEmptyBlockLightMask(@Argument("emptyBlockLightMask") int emptyBlockLightMask) {
        return BitSet.valueOf(new long[] {emptyBlockLightMask & 0x3ffff});
    }
}
