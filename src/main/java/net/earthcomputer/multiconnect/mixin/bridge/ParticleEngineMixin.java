package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.protocols.generic.IParticleManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin implements IParticleManager {

    @Shadow @Final private Map<ResourceLocation, Object> spriteSets;
    @Shadow protected ClientLevel level;

    @Unique private final Map<ParticleType<?>, ParticleProvider<?>> multiconnect_customProviders = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
    private <T extends ParticleOptions> void onCreateParticle(T effect, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, CallbackInfoReturnable<Particle> ci) {
        ParticleProvider<T> customProvider = (ParticleProvider<T>) multiconnect_customProviders.get(effect.getType());
        if (customProvider != null)
            ci.setReturnValue(customProvider.createParticle(effect, level, x, y, z, xSpeed, ySpeed, zSpeed));
    }

    @Override
    public <T extends ParticleOptions> void multiconnect_registerProvider(ParticleType<T> type, ParticleProvider<T> factory) {
        multiconnect_customProviders.put(type, factory);
    }

    @Override
    public <T extends ParticleOptions> void multiconnect_registerSpriteSet(
        ParticleType<T> type,
        Function<SpriteSet, ParticleProvider<T>> spriteAwareFactory
    ) {
        SpriteSet spriteSet = new ParticleEngine.MutableSpriteSet();

        spriteSets.put(Registry.PARTICLE_TYPE.getKey(type), spriteSet);
        multiconnect_customProviders.put(type, spriteAwareFactory.apply(spriteSet));
    }
}
