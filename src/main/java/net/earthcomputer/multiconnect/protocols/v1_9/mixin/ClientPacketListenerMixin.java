package net.earthcomputer.multiconnect.protocols.v1_9.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Unique
    private String lastHash = "";

    @Inject(method = "handleResourcePack", at = @At("HEAD"))
    private void onOnResourcePackSend(ClientboundResourcePackPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_9_4) {
            lastHash = packet.getHash();
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/game/ServerboundResourcePackPacket$Action;)V", at = @At("HEAD"), cancellable = true)
    private void onSendResourcePackStatus(ServerboundResourcePackPacket.Action status, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_9_4) {
            // TODO: rewrite 1.9.4
//            sendPacket(new ResourcePackStatusC2SPacket_1_9_4(lastHash, status));
            ci.cancel();
        }
    }
}
