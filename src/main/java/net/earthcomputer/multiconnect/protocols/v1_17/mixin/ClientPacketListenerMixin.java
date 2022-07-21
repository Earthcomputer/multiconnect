package net.earthcomputer.multiconnect.protocols.v1_17.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Shadow public abstract void handleSetSimulationDistance(ClientboundSetSimulationDistancePacket packet);

    @Inject(method = "handleSetChunkCacheRadius", at = @At("RETURN"))
    private void onOnChunkLoadDistance(ClientboundSetChunkCacheRadiusPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_17_1) {
            handleSetSimulationDistance(new ClientboundSetSimulationDistancePacket(packet.getRadius()));
        }
    }
}
