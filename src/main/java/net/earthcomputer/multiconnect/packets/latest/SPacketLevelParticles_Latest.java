package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.PolymorphicBy;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketLevelParticles;

@MessageVariant(minVersion = Protocols.V1_19)
public class SPacketLevelParticles_Latest implements SPacketLevelParticles {
    @Registry(Registries.PARTICLE_TYPE)
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
