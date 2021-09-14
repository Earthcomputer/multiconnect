package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Protocol;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.v1_16_5.SPacketLightUpdate_1_16_5;

import java.util.BitSet;

@Message(translateFromOlder = @Protocol(value = Protocols.V1_16_5, type = SPacketLightUpdate_1_16_5.class))
public class SPacketLightUpdate {
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
