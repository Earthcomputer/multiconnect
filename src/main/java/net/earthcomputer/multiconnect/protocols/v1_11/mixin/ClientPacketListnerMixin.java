package net.earthcomputer.multiconnect.protocols.v1_11.mixin;

import com.google.common.collect.Collections2;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.protocols.v1_11.AchievementManager;
import net.earthcomputer.multiconnect.protocols.v1_11.PendingAchievements;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.stats.RecipeBookSettings;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListnerMixin {

    @Shadow public abstract void handleAddOrRemoveRecipes(ClientboundRecipePacket packet);

    @Inject(method = "handleLogin", at = @At("RETURN"))
    private void onHandleLogin(CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_11_2) {
            AchievementManager.setToDefault();
        }
    }

    @Inject(method = "handleAwardStats", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V", shift = At.Shift.AFTER))
    private void onHandleAwardStats(ClientboundAwardStatsPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_11_2) {
            PendingAchievements achievements = PacketSystem.getUserData(packet).get(PendingAchievements.KEY);
            AchievementManager.update(achievements.toAdd(), achievements.toRemove());
        }
    }

    @Inject(method = "handleUpdateRecipes", at = @At("TAIL"))
    private void onHandleUpdateRecipes(ClientboundUpdateRecipesPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_11_2) {
            handleAddOrRemoveRecipes(new ClientboundRecipePacket(
                    ClientboundRecipePacket.State.INIT,
                    Collections2.transform(packet.getRecipes(), Recipe::getId),
                    Collections2.transform(packet.getRecipes(), Recipe::getId),
                    new RecipeBookSettings()
            ));
        }
    }

}
