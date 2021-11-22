package net.earthcomputer.multiconnect.protocols.v1_17_1.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.particle.BlockMarkerParticle;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockMarkerParticle.class)
public interface BlockMarkerParticleAccessor {
    @Invoker
    static BlockMarkerParticle createBlockMarkerParticle(ClientWorld world, double x, double y, double z, BlockState state) {
        return MixinHelper.fakeInstance();
    }
}
