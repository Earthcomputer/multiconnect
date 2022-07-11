package net.earthcomputer.multiconnect.protocols.v1_12.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_12.TabCompletionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Shadow @Final private RecipeManager recipeManager;

    @Inject(method = "handleAddOrRemoveRecipes", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V", shift = At.Shift.AFTER))
    private void handleAddOrRemoveRecipes(ClientboundRecipePacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2 && packet.getState() == ClientboundRecipePacket.State.INIT) {
            // ensure recipe lists are mutable
            ClientboundRecipePacketAccessor accessor = (ClientboundRecipePacketAccessor) packet;
            accessor.setToHighlight(new ArrayList<>(packet.getHighlights()));
            accessor.setRecipes(new ArrayList<>(packet.getRecipes()));

            // TODO: move this to network system

            // add smelting recipes
            for (Recipe<?> recipe : recipeManager.getRecipes()) {
                if (recipe.getType() == RecipeType.SMELTING) {
                    if (!packet.getHighlights().contains(recipe.getId())) {
                        packet.getHighlights().add(recipe.getId());
                    }
                    if (!packet.getRecipes().contains(recipe.getId())) {
                        packet.getRecipes().add(recipe.getId());
                    }
                }
            }
        }
    }

    @Inject(method = "handleEntityEvent", at = @At("RETURN"))
    private void onHandleEntityEvent(ClientboundEntityEventPacket packet, CallbackInfo ci) {
        // TODO: move this to network system

        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            assert Minecraft.getInstance().level != null;
            if (packet.getEntity(Minecraft.getInstance().level) == Minecraft.getInstance().player
                    && packet.getEventId() >= 24 && packet.getEventId() <= 28) {
                TabCompletionManager.requestCommandList();
            }
        }
    }

}
