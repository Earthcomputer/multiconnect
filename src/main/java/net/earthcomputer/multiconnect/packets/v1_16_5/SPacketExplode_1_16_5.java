package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketExplode;
import net.earthcomputer.multiconnect.packets.latest.SPacketExplode_Latest;

import java.util.List;

@MessageVariant(maxVersion = Protocols.V1_16_5)
public class SPacketExplode_1_16_5 implements SPacketExplode {
    public float x;
    public float y;
    public float z;
    public float strength;
    @Length(type = Types.INT)
    public List<SPacketExplode_Latest.DestroyedBlock> destroyedBlocks;
    public float playerMotionX;
    public float playerMotionY;
    public float playerMotionZ;
}
