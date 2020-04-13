package net.earthcomputer.multiconnect.impl;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;

import java.util.function.Function;

public interface IParticleManager {

    <T extends IParticleData> void multiconnect_registerFactory(ParticleType<T> type, IParticleFactory<T> factory);

    <T extends IParticleData> void multiconnect_registerSpriteAwareFactory(ParticleType<T> type,
                                                                            Function<IAnimatedSprite, IParticleFactory<T>> spriteAwareFactory);

}
