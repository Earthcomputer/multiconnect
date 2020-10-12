package net.earthcomputer.multiconnect.protocols.v1_9_4.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_9_4.ResourcePackStatusC2SPacket_1_9_4;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.play.ResourcePackSendS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {
    @Shadow public abstract void sendPacket(Packet<?> packet);

    @Unique
    private String lastHash = "";

    @Inject(method = "onResourcePackSend", at = @At("HEAD"))
    private void onOnResourcePackSend(ResourcePackSendS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_9_4) {
            lastHash = packet.getSHA1();
        }
    }

    @Inject(method = "sendResourcePackStatus", at = @At("HEAD"), cancellable = true)
    private void onSendResourcePackStatus(ResourcePackStatusC2SPacket.Status status, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_9_4) {
            sendPacket(new ResourcePackStatusC2SPacket_1_9_4(lastHash, status));
            ci.cancel();
        }
    }
}
