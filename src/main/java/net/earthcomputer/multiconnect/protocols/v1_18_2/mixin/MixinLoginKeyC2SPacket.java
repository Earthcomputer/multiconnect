package net.earthcomputer.multiconnect.protocols.v1_18_2.mixin;

import com.mojang.datafixers.util.Either;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// TODO: fix login packets on the network layer
@Mixin(LoginKeyC2SPacket.class)
public class MixinLoginKeyC2SPacket {
    @Shadow @Final private Either<byte[], NetworkEncryptionUtils.SignatureData> nonce;

    @Inject(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeEither(Lcom/mojang/datafixers/util/Either;Lnet/minecraft/network/PacketByteBuf$PacketWriter;Lnet/minecraft/network/PacketByteBuf$PacketWriter;)V"), cancellable = true)
    private void onWriteNonce(PacketByteBuf buf, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_18_2) {
            nonce.ifLeft(buf::writeByteArray);
            ci.cancel();
        }
    }
}
