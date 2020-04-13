package net.earthcomputer.multiconnect.protocols.v1_12_2;

import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleType;

public class MyBlockStateParticleType extends ParticleType<BlockParticleData> {

    protected MyBlockStateParticleType(boolean ignoreRange) {
        super(ignoreRange, BlockParticleData.DESERIALIZER);
    }
}
