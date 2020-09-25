package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class MixinBlock {
    @Inject(method = "getBlastResistance", at = @At("RETURN"), cancellable = true)
    private void modifyBlastResistance(CallbackInfoReturnable<Float> ci) {
        ci.setReturnValue(ConnectionInfo.protocol.getBlockResistance((Block) (Object) this, ci.getReturnValueF()));
    }
}
