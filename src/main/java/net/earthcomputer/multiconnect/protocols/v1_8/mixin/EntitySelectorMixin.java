package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntitySelector.class)
public class EntitySelectorMixin {
    @SuppressWarnings("target")
    @Redirect(method = "method_5915(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/scores/Team;Lnet/minecraft/world/scores/Team$CollisionRule;Lnet/minecraft/world/entity/Entity;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isLocalPlayer()Z"))
    private static boolean makeMainPlayerUnpushable(Player player) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            return false;
        }
        return player.isLocalPlayer();
    }
}
