package net.earthcomputer.multiconnect.protocols.v1_12_2;

import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleType;

public class MyBlockStateParticleType extends ParticleType<BlockStateParticleEffect> {

    protected MyBlockStateParticleType(boolean ignoreRange) {
        super(ignoreRange, BlockStateParticleEffect.PARAMETERS_FACTORY, BlockStateParticleEffect.field_23633);
    }
}
