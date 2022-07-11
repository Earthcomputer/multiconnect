package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_8.IClientPlayer;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    @Inject(method = "attack", at = @At("HEAD"))
    private void onAttackEntity(Player player, Entity target, CallbackInfo ci) {
        // The hand swing and attack packets are the other way around in 1.8. Fixes anti-cheat triggering
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            player.swing(InteractionHand.MAIN_HAND);
            ((IClientPlayer) player).multiconnect_cancelSwingPacket();
        }
    }
}
