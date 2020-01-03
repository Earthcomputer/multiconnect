package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class MixinClientWorld {

    @Inject(method = "setBlockStateWithoutNeighborUpdates", at = @At("HEAD"), cancellable = true)
    private void onSetBlockWithoutNeighborUpdates(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            ((ClientWorld) (Object) this).setBlockState(pos, state);
            ci.cancel();
        }
    }

}
