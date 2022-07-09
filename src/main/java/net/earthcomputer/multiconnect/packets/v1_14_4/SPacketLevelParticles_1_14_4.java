package net.earthcomputer.multiconnect.packets.v1_14_4;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.PolymorphicBy;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketLevelParticles;
import net.earthcomputer.multiconnect.packets.v1_12_2.Particle_1_12_2;

import java.util.function.Function;

@MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_14_4)
public class SPacketLevelParticles_1_14_4 implements SPacketLevelParticles {
    @Registry(Registries.PARTICLE_TYPE)
    @Type(Types.INT)
    public int particleId;
    public boolean longDistance;
    public float x;
    public float y;
    public float z;
    @Introduce(compute = "computeOffsetX")
    public float offsetX;
    @Introduce(compute = "computeOffsetY")
    public float offsetY;
    @Introduce(compute = "computeOffsetZ")
    public float offsetZ;
    public float particleData;
    @Type(Types.INT)
    public int count;
    @PolymorphicBy(field = "particleId")
    @Introduce(compute = "computeParticle")
    public CommonTypes.Particle particle;

    public static float computeOffsetX(
            @Argument("offsetX") float offsetX,
            @Argument("particle") CommonTypes.Particle particle
    ) {
        return particle instanceof CommonTypes.Particle.Dust ? 0 : offsetX;
    }

    public static float computeOffsetY(
            @Argument("offsetY") float offsetY,
            @Argument("particle") CommonTypes.Particle particle
    ) {
        return particle instanceof CommonTypes.Particle.Dust ? 0 : offsetY;
    }

    public static float computeOffsetZ(
            @Argument("offsetZ") float offsetZ,
            @Argument("particle") CommonTypes.Particle particle
    ) {
        return particle instanceof CommonTypes.Particle.Dust ? 0 : offsetZ;
    }

    public static CommonTypes.Particle computeParticle(
            @Argument("offsetX") float offsetX,
            @Argument("offsetY") float offsetY,
            @Argument("offsetZ") float offsetZ,
            @Argument("particle") CommonTypes.Particle particle,
            @FilledArgument(fromVersion = Protocols.V1_12_2, toVersion = Protocols.V1_13) Function<Particle_1_12_2, CommonTypes.Particle_Latest> particleTranslator
    ) {
        var newParticle = particleTranslator.apply((Particle_1_12_2) particle);
        if (newParticle instanceof CommonTypes.Particle_Latest.Dust dust) {
            dust.red = offsetX;
            dust.green = offsetY;
            dust.blue = offsetZ;
        }
        return newParticle;
    }
}
