package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.IParticleManager;
import net.earthcomputer.multiconnect.protocols.AbstractProtocol;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
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
                    .filter(cls -> ArrayUtils.contains(cls.getInterfaces(), IAnimatedSprite.class))
                    .findFirst().orElseThrow(ClassNotFoundException::new);
            SSP_CTOR = ssp.getDeclaredConstructor(ParticleManager.class);
            SSP_CTOR.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    @Shadow @Final private Map<ResourceLocation, Object> sprites;
    @Shadow protected World world;

    @Unique private Map<ResourceLocation, IParticleFactory<?>> customFactories = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Inject(method = "makeParticle", at = @At("HEAD"), cancellable = true)
    private <T extends IParticleData> void onCreateParticle(T effect, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, CallbackInfoReturnable<Particle> ci) {
        IParticleFactory<T> customFactory = (IParticleFactory<T>) customFactories.get(Registry.PARTICLE_TYPE.getKey(effect.getType()));
        if (customFactory != null)
            ci.setReturnValue(customFactory.makeParticle(effect, world, x, y, z, xSpeed, ySpeed, zSpeed));
    }

    /*@Redirect(method = "makeParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/Registry;getRawId(Ljava/lang/Object;)I"))
    private int redirectRawId(Registry<ParticleType<?>> registry, Object type) {
        return AbstractProtocol.getUnmodifiedId(registry, (ParticleType<?>) type);
    }*/

    @ModifyVariable(method = "loadTextureLists", ordinal = 0, at = @At("HEAD"))
    private ResourceLocation modifyResourceLocation(ResourceLocation id) {
        ResourceLocation unmodifiedName = AbstractProtocol.getUnmodifiedName(Registry.PARTICLE_TYPE, Registry.PARTICLE_TYPE.getOrDefault(id));
        return unmodifiedName == null ? id : unmodifiedName;
    }

    @Override
    public <T extends IParticleData> void multiconnect_registerFactory(ParticleType<T> type, IParticleFactory<T> factory) {
        customFactories.put(Registry.PARTICLE_TYPE.getKey(type), factory);
    }

    @Override
    public <T extends IParticleData> void multiconnect_registerSpriteAwareFactory(ParticleType<T> type,
                                                                                   Function<IAnimatedSprite, IParticleFactory<T>> spriteAwareFactory) {
        // https://stackoverflow.com/questions/26775676/explicit-use-of-lambdametafactory
        IAnimatedSprite spriteProvider;
        try {
            spriteProvider = (IAnimatedSprite) SSP_CTOR.newInstance((ParticleManager) (Object) this);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }

        ResourceLocation id = Registry.PARTICLE_TYPE.getKey(type);
        sprites.put(id, spriteProvider);
        customFactories.put(id, spriteAwareFactory.apply(spriteProvider));
    }
}
