package net.earthcomputer.multiconnect.protocols.v1_12_2;

import com.mojang.serialization.Codec;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleType;

public class MyBlockStateParticleType extends ParticleType<BlockStateParticleEffect> {

    protected MyBlockStateParticleType(boolean ignoreRange) {
        super(ignoreRange, BlockStateParticleEffect.PARAMETERS_FACTORY);
    }

    @Override
    public Codec<BlockStateParticleEffect> method_29138() {
        return BlockStateParticleEffect.method_29128(this);
    }
}
