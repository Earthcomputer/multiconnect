package net.earthcomputer.multiconnect.protocols.v1_12.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BreakingItemParticle.class)
public interface BreakingItemParticleAccessor {

    @Invoker("<init>")
    static BreakingItemParticle constructor(ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ, ItemStack stack) {
        return MixinHelper.fakeInstance();
    }

}
