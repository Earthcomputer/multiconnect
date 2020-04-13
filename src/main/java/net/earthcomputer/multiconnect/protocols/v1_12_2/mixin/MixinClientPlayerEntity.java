package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {

    @Redirect(method = "livingTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/player/ClientPlayerEntity;isInWater()Z"))
    private boolean allowSprintingInWater(ClientPlayerEntity self) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            return false; // disable all water related movement
        }
        return self.isInWater();
    }

}
