package net.earthcomputer.multiconnect.protocols.v1_11_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_11_2.AchievementManager;
import net.earthcomputer.multiconnect.protocols.v1_11_2.IScreenHandler;
import net.earthcomputer.multiconnect.protocols.v1_11_2.PendingAchievements;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ConfirmGuiActionS2CPacket;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    @Inject(method = "onGameJoin", at = @At("RETURN"))
    private void onOnGameJoin(CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_11_2) {
            AchievementManager.setToDefault();
        }
    }

    @Inject(method = "onStatistics", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER))
    private void onOnStatistics(CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_11_2) {
            PendingAchievements achievements = PendingAchievements.poll();
            AchievementManager.update(achievements.getToAdd(), achievements.getToRemove());
        }
    }

    @Inject(method = "onGuiActionConfirm", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/ConfirmGuiActionS2CPacket;wasAccepted()Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onOnGuiActionConfirm(ConfirmGuiActionS2CPacket packet, CallbackInfo ci, ScreenHandler screenHandler) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_11_2) {
            ((IScreenHandler) screenHandler).multiconnect_getRecipeBookEmulator().onConfirmTransaction(packet);
        }
    }

}
