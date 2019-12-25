package net.earthcomputer.multiconnect.impl;

import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

import java.util.function.Function;

public interface IParticleManager {

    <T extends ParticleEffect> void multiconnect_registerFactory(ParticleType<T> type, ParticleFactory<T> factory);

    <T extends ParticleEffect> void multiconnect_registerSpriteAwareFactory(ParticleType<T> type,
                                                                            Function<SpriteProvider, ParticleFactory<T>> spriteAwareFactory);

}
