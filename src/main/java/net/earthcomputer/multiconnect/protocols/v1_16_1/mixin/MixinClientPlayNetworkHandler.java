package net.earthcomputer.multiconnect.protocols.v1_16_1.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.s2c.play.UnlockRecipesS2CPacket;
import net.minecraft.recipe.book.RecipeBook;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.book.RecipeBookOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {
    @Inject(method = "onUnlockRecipes", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER))
    private void onOnUnlockRecipes(UnlockRecipesS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_16_1) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            assert player != null;

            // Copy over old options that weren't sent on-thread to avoid a race condition
            RecipeBook oldRecipeBook = player.getRecipeBook();
            RecipeBookOptions newOptions = packet.getOptions();

            newOptions.setGuiOpen(RecipeBookCategory.BLAST_FURNACE, oldRecipeBook.isGuiOpen(RecipeBookCategory.BLAST_FURNACE));
            newOptions.setFilteringCraftable(RecipeBookCategory.BLAST_FURNACE, oldRecipeBook.isFilteringCraftable(RecipeBookCategory.BLAST_FURNACE));
            newOptions.setGuiOpen(RecipeBookCategory.SMOKER, oldRecipeBook.isGuiOpen(RecipeBookCategory.SMOKER));
            newOptions.setFilteringCraftable(RecipeBookCategory.SMOKER, oldRecipeBook.isFilteringCraftable(RecipeBookCategory.SMOKER));
        }
    }
}
