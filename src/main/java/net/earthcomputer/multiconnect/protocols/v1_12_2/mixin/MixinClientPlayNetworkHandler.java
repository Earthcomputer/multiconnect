package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_12_2.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {
    @Shadow @Final private RecipeManager recipeManager;

    @Inject(method = "onUnlockRecipes", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER))
    private void onOnUnlockRecipes(UnlockRecipesS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2 && packet.getAction() == UnlockRecipesS2CPacket.Action.INIT) {
            // ensure recipe lists are mutable
            UnlockRecipesS2CAccessor accessor = (UnlockRecipesS2CAccessor) packet;
            accessor.setRecipeIdsToInit(new ArrayList<>(packet.getRecipeIdsToInit()));
            accessor.setRecipeIdsToChange(new ArrayList<>(packet.getRecipeIdsToChange()));

            // TODO: move this to network system

            // add smelting recipes
            for (Recipe<?> recipe : recipeManager.values()) {
                if (recipe.getType() == RecipeType.SMELTING) {
                    if (!packet.getRecipeIdsToInit().contains(recipe.getId())) {
                        packet.getRecipeIdsToInit().add(recipe.getId());
                    }
                    if (!packet.getRecipeIdsToChange().contains(recipe.getId())) {
                        packet.getRecipeIdsToChange().add(recipe.getId());
                    }
                }
            }
        }
    }

    @Inject(method = "onEntityStatus", at = @At("RETURN"))
    private void onOnEntityStatus(EntityStatusS2CPacket packet, CallbackInfo ci) {
        // TODO: move this to network system

        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            assert MinecraftClient.getInstance().world != null;
            if (packet.getEntity(MinecraftClient.getInstance().world) == MinecraftClient.getInstance().player
                    && packet.getStatus() >= 24 && packet.getStatus() <= 28) {
                TabCompletionManager.requestCommandList();
            }
        }
    }

}
