package net.earthcomputer.multiconnect.mixin.debug;

import net.earthcomputer.multiconnect.debug.PacketRecorder;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {
    @Inject(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"))
    private void onSendMovementPackets(CallbackInfo ci) {
        PacketRecorder.tickMovement((ClientPlayerEntity) (Object) this);
    }
}
