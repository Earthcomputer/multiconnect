package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.protocols.generic.IClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientPlayNetworkHandler.class, priority = 10000)
public class MixinClientPlayNetworkHandlerHighPriority {
    @Inject(method = "onChunkData", at = @At("TAIL"))
    private void afterOnChunkData(ChunkDataS2CPacket packet, CallbackInfo ci) {
        ((IClientPlayNetworkHandler) this).multiconnect_onAfterChunkData(packet);
    }
}
