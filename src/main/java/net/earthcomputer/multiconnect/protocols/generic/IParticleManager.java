package net.earthcomputer.multiconnect.protocols.generic;

import java.util.function.Function;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

public interface IParticleManager {

    <T extends ParticleOptions> void multiconnect_registerProvider(ParticleType<T> type, ParticleProvider<T> factory);

    <T extends ParticleOptions> void multiconnect_registerSpriteSet(ParticleType<T> type,
                                                                    Function<SpriteSet, ParticleProvider<T>> spriteAwareFactory);

}
