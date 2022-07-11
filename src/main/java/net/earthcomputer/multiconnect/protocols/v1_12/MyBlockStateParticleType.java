package net.earthcomputer.multiconnect.protocols.v1_12;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleType;

public class MyBlockStateParticleType extends ParticleType<BlockParticleOption> {

    protected MyBlockStateParticleType(boolean ignoreRange) {
        super(ignoreRange, BlockParticleOption.DESERIALIZER);
    }

    @Override
    public Codec<BlockParticleOption> codec() {
        return BlockParticleOption.codec(this);
    }
}
