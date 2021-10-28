package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketExplosion;

import java.util.List;

@Message(variantOf = SPacketExplosion.class, maxVersion = Protocols.V1_16_5)
public class SPacketExplosion_1_16_5 {
    public float x;
    public float y;
    public float z;
    public float strength;
    @Length(type = Types.INT)
    public List<SPacketExplosion.DestroyedBlock> destroyedBlocks;
    public float playerMotionX;
    public float playerMotionY;
    public float playerMotionZ;
}
