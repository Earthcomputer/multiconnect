package net.earthcomputer.multiconnect.protocols.v1_18.mixin;

import com.mojang.datafixers.util.Either;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.util.Crypt;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// TODO: fix login packets on the network layer
@Mixin(ServerboundKeyPacket.class)
public class ServerboundKeyPacketMixin {
    @Shadow @Final private Either<byte[], Crypt.SaltSignaturePair> nonceOrSaltSignature;

    @Inject(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;writeEither(Lcom/mojang/datafixers/util/Either;Lnet/minecraft/network/FriendlyByteBuf$Writer;Lnet/minecraft/network/FriendlyByteBuf$Writer;)V"), cancellable = true)
    private void onWriteNonce(FriendlyByteBuf buf, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_18_2) {
            nonceOrSaltSignature.ifLeft(buf::writeByteArray);
            ci.cancel();
        }
    }
}
