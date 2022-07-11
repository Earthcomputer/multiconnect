package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
    @Shadow protected abstract BlockState asState();

    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    private void modifyDestroySpeed(CallbackInfoReturnable<Float> ci) {
        ci.setReturnValue(ConnectionInfo.protocol.getBlockDestroySpeed(this.asState(), ci.getReturnValueF()));
    }
}
