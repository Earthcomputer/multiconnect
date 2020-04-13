package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.client.particle.BreakingParticle;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BreakingParticle.class)
public interface CrackParticleAccessor {

    @Invoker("<init>")
    static BreakingParticle constructor(World world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, ItemStack stack) {
        return MixinHelper.fakeInstance();
    }

}
