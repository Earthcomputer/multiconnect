package net.earthcomputer.multiconnect.protocols.v1_12.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SuspendedTownParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SuspendedTownParticle.class)
public interface SuspendTownParticleAccessor {

    @Invoker("<init>")
    static SuspendedTownParticle constructor(ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        return MixinHelper.fakeInstance();
    }

}
