package net.earthcomputer.multiconnect.packets.v1_14_4;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.PolymorphicBy;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketParticle;

@MessageVariant(maxVersion = Protocols.V1_14_4)
public class SPacketParticle_1_14_4 implements SPacketParticle {
    @Registry(Registries.PARTICLE_TYPE)
    @Type(Types.INT)
    public int particleId;
    public boolean longDistance;
    public float x;
    public float y;
    public float z;
    public float offsetX;
    public float offsetY;
    public float offsetZ;
    public float particleData;
    @Type(Types.INT)
    public int count;
    @PolymorphicBy(field = "particleId")
    public CommonTypes.Particle particle;
}
