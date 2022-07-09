package net.earthcomputer.multiconnect.protocols.v1_16.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.packets.v1_16_5.SPacketMapUpdate_1_16_5;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.RecipeBookSettings;
import net.minecraft.world.inventory.RecipeBookType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handleAddOrRemoveRecipes", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V", shift = At.Shift.AFTER))
    private void onHandleAddOrRemoveRecipes(ClientboundRecipePacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_16_1) {
            LocalPlayer player = Minecraft.getInstance().player;
            assert player != null;

            // Copy over old options that weren't sent on-thread to avoid a race condition
            RecipeBook oldRecipeBook = player.getRecipeBook();
            RecipeBookSettings newOptions = packet.getBookSettings();

            newOptions.setOpen(RecipeBookType.BLAST_FURNACE, oldRecipeBook.isOpen(RecipeBookType.BLAST_FURNACE));
            newOptions.setFiltering(RecipeBookType.BLAST_FURNACE, oldRecipeBook.isFiltering(RecipeBookType.BLAST_FURNACE));
            newOptions.setOpen(RecipeBookType.SMOKER, oldRecipeBook.isOpen(RecipeBookType.SMOKER));
            newOptions.setFiltering(RecipeBookType.SMOKER, oldRecipeBook.isFiltering(RecipeBookType.SMOKER));
        }
    }

    @Inject(method = "handleMapItemData", at = @At("RETURN"))
    private void onHandleMapItemData(ClientboundMapItemDataPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_16_5) {
            Runnable runnable = PacketSystem.getUserData(packet).get(SPacketMapUpdate_1_16_5.POST_HANDLE_MAP_PACKET);
            if (runnable != null) {
                runnable.run();
            }
        }
    }
}
