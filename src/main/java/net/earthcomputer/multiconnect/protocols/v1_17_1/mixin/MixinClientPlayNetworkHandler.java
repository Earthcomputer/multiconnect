package net.earthcomputer.multiconnect.protocols.v1_17_1.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ChunkLoadDistanceS2CPacket;
import net.minecraft.network.packet.s2c.play.SimulationDistanceS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {
    @Shadow public abstract void onSimulationDistance(SimulationDistanceS2CPacket packet);

    @Inject(method = "onChunkLoadDistance", at = @At("RETURN"))
    private void onOnChunkLoadDistance(ChunkLoadDistanceS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_17_1) {
            onSimulationDistance(new SimulationDistanceS2CPacket(packet.getDistance()));
        }
    }
}
