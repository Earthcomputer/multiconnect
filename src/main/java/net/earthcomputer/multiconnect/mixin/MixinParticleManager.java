package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.protocols.AbstractProtocol;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ParticleManager.class)
public class MixinParticleManager {

    @Redirect(method = "createParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/Registry;getRawId(Ljava/lang/Object;)I"))
    private int redirectRawId(Registry<ParticleType<?>> registry, Object type) {
        return AbstractProtocol.getUnmodifiedId(registry, (ParticleType<?>) type);
    }

}
