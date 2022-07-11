package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "getExplosionResistance", at = @At("RETURN"), cancellable = true)
    private void modifyExplosionResistance(CallbackInfoReturnable<Float> ci) {
        ci.setReturnValue(ConnectionInfo.protocol.getBlockExplosionResistance((Block) (Object) this, ci.getReturnValueF()));
    }
}
