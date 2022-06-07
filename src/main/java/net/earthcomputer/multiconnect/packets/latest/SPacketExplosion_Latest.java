package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketExplosion;

import java.util.List;

@MessageVariant(minVersion = Protocols.V1_17)
public class SPacketExplosion_Latest implements SPacketExplosion {
    public float x;
    public float y;
    public float z;
    public float strength;
    public List<DestroyedBlock> destroyedBlocks;
    public float playerMotionX;
    public float playerMotionY;
    public float playerMotionZ;

    @MessageVariant
    public static class DestroyedBlock {
        public byte x;
        public byte y;
        public byte z;
    }
}
