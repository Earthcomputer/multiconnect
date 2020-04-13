package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.CurrentChunkDataPacket;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.play.server.SChunkDataPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetHandler.class)
public class MixinClientPlayNetworkHandler {

    @Inject(method = "handleChunkData", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketThreadUtil;checkThreadAndEnqueue(Lnet/minecraft/network/IPacket;Lnet/minecraft/network/INetHandler;Lnet/minecraft/util/concurrent/ThreadTaskExecutor;)V", shift = At.Shift.AFTER))
    private void preChunkData(SChunkDataPacket packet, CallbackInfo ci) {
        CurrentChunkDataPacket.push(packet);
    }

    @Inject(method = "handleChunkData", at = @At("RETURN"))
    private void postChunkData(SChunkDataPacket packet, CallbackInfo ci) {
        CurrentChunkDataPacket.pop();
    }

}
