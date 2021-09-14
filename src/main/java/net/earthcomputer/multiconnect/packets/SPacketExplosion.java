package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

import java.util.List;

@Message
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
