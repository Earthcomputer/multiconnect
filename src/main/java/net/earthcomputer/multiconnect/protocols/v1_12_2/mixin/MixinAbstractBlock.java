package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.block.AbstractBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public class MixinAbstractBlock {
    @Inject(method = "hasBlockEntity", at = @At("HEAD"))
    public void hasBlockEntity(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(ConnectionInfo.protocolVersion <= Protocols.V1_12_2 || cir.getReturnValue());
    }
}
