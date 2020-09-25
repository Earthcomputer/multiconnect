package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class MixinAbstractBlockState {
    @Shadow protected abstract BlockState asBlockState();

    @Inject(method = "getHardness", at = @At("RETURN"), cancellable = true)
    private void modifyHardness(CallbackInfoReturnable<Float> ci) {
        ci.setReturnValue(ConnectionInfo.protocol.getBlockHardness(this.asBlockState(), ci.getReturnValueF()));
    }
}
