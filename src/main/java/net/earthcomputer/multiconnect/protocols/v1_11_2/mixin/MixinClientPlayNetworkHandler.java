package net.earthcomputer.multiconnect.protocols.v1_11_2.mixin;

import com.google.common.collect.Collections2;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.generic.IUserDataHolder;
import net.earthcomputer.multiconnect.protocols.v1_11_2.AchievementManager;
import net.earthcomputer.multiconnect.protocols.v1_11_2.PendingAchievements;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.network.packet.s2c.play.UnlockRecipesS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeBookOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {

    @Shadow public abstract void onUnlockRecipes(UnlockRecipesS2CPacket packet);

    @Inject(method = "onGameJoin", at = @At("RETURN"))
    private void onOnGameJoin(CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_11_2) {
            AchievementManager.setToDefault();
        }
    }

    @Inject(method = "onStatistics", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER))
    private void onOnStatistics(StatisticsS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_11_2) {
            PendingAchievements achievements = ((IUserDataHolder) packet).multiconnect_getUserData(PendingAchievements.KEY);
            AchievementManager.update(achievements.toAdd(), achievements.toRemove());
        }
    }

    @Inject(method = "onSynchronizeRecipes", at = @At("TAIL"))
    private void onOnSynchronizeRecipes(SynchronizeRecipesS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_11_2) {
            onUnlockRecipes(new UnlockRecipesS2CPacket(
                    UnlockRecipesS2CPacket.Action.INIT,
                    Collections2.transform(packet.getRecipes(), Recipe::getId),
                    Collections2.transform(packet.getRecipes(), Recipe::getId),
                    new RecipeBookOptions()
            ));
        }
    }

}
