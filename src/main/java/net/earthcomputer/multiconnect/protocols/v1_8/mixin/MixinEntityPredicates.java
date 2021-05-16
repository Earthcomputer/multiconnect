package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityPredicates.class)
public class MixinEntityPredicates {
    @Dynamic
    @Redirect(method = "method_5915", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isMainPlayer()Z"))
    private static boolean makeMainPlayerUnpushable(PlayerEntity player) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            return false;
        }
        return player.isMainPlayer();
    }
}
