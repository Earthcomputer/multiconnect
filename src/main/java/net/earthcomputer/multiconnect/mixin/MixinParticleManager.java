package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.IParticleManager;
import net.earthcomputer.multiconnect.protocols.AbstractProtocol;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Mixin(ParticleManager.class)
public class MixinParticleManager implements IParticleManager {

    @Unique private static final Constructor<?> SSP_CTOR;
    static {
        try {
            Class<?> ssp = Arrays.stream(ParticleManager.class.getDeclaredClasses())
                    .filter(cls -> ArrayUtils.contains(cls.getInterfaces(), SpriteProvider.class))
                    .findFirst().orElseThrow(ClassNotFoundException::new);
            SSP_CTOR = ssp.getDeclaredConstructor(ParticleManager.class);
            SSP_CTOR.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    @Shadow @Final private Map<Identifier, Object> spriteAwareFactories;
    @Shadow protected ClientWorld world;

    @Unique private Map<Identifier, ParticleFactory<?>> customFactories = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
    private <T extends ParticleEffect> void onCreateParticle(T effect, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, CallbackInfoReturnable<Particle> ci) {
        ParticleFactory<T> customFactory = (ParticleFactory<T>) customFactories.get(Registry.PARTICLE_TYPE.getId(effect.getType()));
        if (customFactory != null)
            ci.setReturnValue(customFactory.createParticle(effect, world, x, y, z, xSpeed, ySpeed, zSpeed));
    }

    @Redirect(method = "createParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/Registry;getRawId(Ljava/lang/Object;)I"))
    private int redirectRawId(Registry<ParticleType<?>> registry, Object type) {
        return AbstractProtocol.getUnmodifiedId(registry, (ParticleType<?>) type);
    }

    @ModifyVariable(method = "loadTextureList", ordinal = 0, at = @At("HEAD"))
    private Identifier modifyIdentifier(Identifier id) {
        Identifier unmodifiedName = AbstractProtocol.getUnmodifiedName(Registry.PARTICLE_TYPE, Registry.PARTICLE_TYPE.get(id));
        return unmodifiedName == null ? id : unmodifiedName;
    }

    @Override
    public <T extends ParticleEffect> void multiconnect_registerFactory(ParticleType<T> type, ParticleFactory<T> factory) {
        customFactories.put(Registry.PARTICLE_TYPE.getId(type), factory);
    }

    @Override
    public <T extends ParticleEffect> void multiconnect_registerSpriteAwareFactory(ParticleType<T> type,
                                                                                   Function<SpriteProvider, ParticleFactory<T>> spriteAwareFactory) {
        // https://stackoverflow.com/questions/26775676/explicit-use-of-lambdametafactory
        SpriteProvider spriteProvider;
        try {
            spriteProvider = (SpriteProvider) SSP_CTOR.newInstance((ParticleManager) (Object) this);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }

        Identifier id = Registry.PARTICLE_TYPE.getId(type);
        spriteAwareFactories.put(id, spriteProvider);
        customFactories.put(id, spriteAwareFactory.apply(spriteProvider));
    }
}
