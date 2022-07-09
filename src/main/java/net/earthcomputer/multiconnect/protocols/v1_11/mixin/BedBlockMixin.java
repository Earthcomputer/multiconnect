package net.earthcomputer.multiconnect.protocols.v1_11.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.BedBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BedBlock.class)
public class BedBlockMixin {

    @Inject(method = "bounceUp", at = @At("HEAD"), cancellable = true)
    public void dontBounceUp(Entity entity, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_11_2) {
            ci.cancel();
        }
    }

}
