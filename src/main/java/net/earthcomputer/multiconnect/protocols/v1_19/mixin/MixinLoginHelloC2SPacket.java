package net.earthcomputer.multiconnect.protocols.v1_19.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

// TODO: fix login packets on the network layer
@Mixin(LoginHelloC2SPacket.class)
public class MixinLoginHelloC2SPacket {
    @Inject(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeOptional(Ljava/util/Optional;Lnet/minecraft/network/PacketByteBuf$PacketWriter;)V"), cancellable = true)
    private void onWritePublicKey(PacketByteBuf buf, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_19) {
            if (ConnectionInfo.protocolVersion > Protocols.V1_18_2) {
                buf.writeOptional(Optional.empty(), (buf1, val) -> {});
            }
            ci.cancel();
        }
    }
}
