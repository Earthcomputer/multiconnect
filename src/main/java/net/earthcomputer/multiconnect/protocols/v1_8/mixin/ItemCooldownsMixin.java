package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.world.item.ItemCooldowns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemCooldowns.class)
public class ItemCooldownsMixin {
    @Inject(method = "addCooldown", at = @At("HEAD"), cancellable = true)
    private void cancelCooldowns(CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            ci.cancel();
        }
    }
}
