package net.earthcomputer.multiconnect.protocols.v1_18.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// TODO: fix login packets on the network layer
@Mixin(ServerboundHelloPacket.class)
public class ServerboundHelloPacketMixin {
    @Inject(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;writeOptional(Ljava/util/Optional;Lnet/minecraft/network/FriendlyByteBuf$Writer;)V"), cancellable = true)
    private void onWritePublicKey(CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_18_2) {
            ci.cancel();
        }
    }
}
