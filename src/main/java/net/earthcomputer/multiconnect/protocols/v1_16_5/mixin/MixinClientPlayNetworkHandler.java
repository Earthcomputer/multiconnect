package net.earthcomputer.multiconnect.protocols.v1_16_5.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.packets.v1_16_5.SPacketMapUpdate_1_16_5;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.MapUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {
    @Inject(method = "onMapUpdate", at = @At("RETURN"))
    private void onOnMapUpdate(MapUpdateS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_16_5) {
            Runnable runnable = PacketSystem.getUserData(packet).get(SPacketMapUpdate_1_16_5.POST_HANDLE_MAP_PACKET);
            if (runnable != null) {
                runnable.run();
            }
        }
    }
}
