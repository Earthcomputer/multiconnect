package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

import java.util.BitSet;

@Message
public class SPacketLightUpdate {
    public int chunkX;
    public int chunkZ;
    public boolean trustEdges;
    public BitSet skyLightMask;
    public BitSet blockLightMask;
    public BitSet emptySkyLightMask;
    public BitSet emptyBlockLightMask;
    public byte[][] skyLightArrays;
    public byte[][] blockLightArrays;
}
