package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.SwordItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow public LocalPlayer player;

    @Redirect(method = "startUseItem",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;useItem(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;itemUsed(Lnet/minecraft/world/InteractionHand;)V", ordinal = 0))
    private void redirectItemUsed(ItemInHandRenderer heldItemRenderer, InteractionHand hand) {
        if (ConnectionInfo.protocolVersion > Protocols.V1_8 || !(player.getItemInHand(hand).getItem() instanceof SwordItem)) {
            heldItemRenderer.itemUsed(hand);
        }
    }
}
