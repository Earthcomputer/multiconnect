package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.client.particle.SuspendParticle;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SuspendParticle.class)
public interface SuspendParticleAccessor {

    @Invoker("<init>")
    static SuspendParticle constructor(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        return MixinHelper.fakeInstance();
    }

}
