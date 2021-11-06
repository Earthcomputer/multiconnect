package net.earthcomputer.multiconnect.protocols.v1_11_2.mixin;

import net.minecraft.block.BedBlock;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BedBlock.class)
public class MixinBedBlock {

    @Inject(method = "bounceEntity", at = @At("HEAD"), cancellable = true)
    public void dontBounceEntity(Entity entity, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_11_2) {
            ci.cancel();
        }
    }

}
