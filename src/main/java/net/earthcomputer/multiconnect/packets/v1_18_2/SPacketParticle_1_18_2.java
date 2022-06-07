package net.earthcomputer.multiconnect.packets.v1_18_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.PolymorphicBy;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketParticle;

@MessageVariant(minVersion = Protocols.V1_15, maxVersion = Protocols.V1_18_2)
public class SPacketParticle_1_18_2 implements SPacketParticle {
    @Registry(Registries.PARTICLE_TYPE)
    @Type(Types.INT)
    public int particleId;
    public boolean longDistance;
    public double x;
    public double y;
    public double z;
    public float offsetX;
    public float offsetY;
    public float offsetZ;
    public float particleData;
    @Type(Types.INT)
    public int count;
    @PolymorphicBy(field = "particleId")
    public CommonTypes.Particle particle;
}
