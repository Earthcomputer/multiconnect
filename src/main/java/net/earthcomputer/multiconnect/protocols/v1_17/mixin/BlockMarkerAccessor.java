package net.earthcomputer.multiconnect.protocols.v1_17.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BlockMarker;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockMarker.class)
public interface BlockMarkerAccessor {
    @Invoker("<init>")
    static BlockMarker createBlockMarkerParticle(ClientLevel level, double x, double y, double z, BlockState state) {
        return MixinHelper.fakeInstance();
    }
}
