package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Protocol;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.v1_16_5.SPacketExplosion_1_16_5;

import java.util.List;

@Message(translateFromOlder = @Protocol(value = Protocols.V1_16_5, type = SPacketExplosion_1_16_5.class))
public class SPacketExplosion {
    public float x;
    public float y;
    public float z;
    public float strength;
    public List<DestroyedBlock> destroyedBlocks;
    public float playerMotionX;
    public float playerMotionY;
    public float playerMotionZ;

    @Message
    public static class DestroyedBlock {
        public byte x;
        public byte y;
        public byte z;
    }
}
