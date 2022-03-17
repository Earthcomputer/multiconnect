package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketExplosion;
import net.earthcomputer.multiconnect.packets.latest.SPacketExplosion_Latest;

import java.util.List;

@MessageVariant(maxVersion = Protocols.V1_16_5)
public class SPacketExplosion_1_16_5 implements SPacketExplosion {
    public float x;
    public float y;
    public float z;
    public float strength;
    @Length(type = Types.INT)
    public List<SPacketExplosion_Latest.DestroyedBlock> destroyedBlocks;
    public float playerMotionX;
    public float playerMotionY;
    public float playerMotionZ;
}
