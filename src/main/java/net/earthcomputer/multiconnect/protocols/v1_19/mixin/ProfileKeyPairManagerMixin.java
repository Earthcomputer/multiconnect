package net.earthcomputer.multiconnect.protocols.v1_19.mixin;

import com.mojang.authlib.yggdrasil.response.KeyPairResponse;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(ProfileKeyPairManager.class)
public abstract class ProfileKeyPairManagerMixin {

    @Shadow public abstract Optional<ProfilePublicKey> profilePublicKey();

    @Inject(method = "preparePublicKey", at = @At("HEAD"), cancellable = true)
    public void revertUpdate(CallbackInfoReturnable<CompletableFuture<Optional<ProfilePublicKey.Data>>> cir) {
        if(ConnectionInfo.protocolVersion <= Protocols.V1_19) {
            cir.setReturnValue(CompletableFuture.supplyAsync(() -> this.profilePublicKey().map(ProfilePublicKey::data)));
        }
    }

    @Redirect(method = "parsePublicKey", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/yggdrasil/response/KeyPairResponse;getPublicKeySignature()Ljava/nio/ByteBuffer;", remap = false))
    private static ByteBuffer useLegacyKey(KeyPairResponse instance) {
        if(ConnectionInfo.protocolVersion <= Protocols.V1_19) {
            return instance.getLegacyPublicKeySignature();
        }
        return instance.getPublicKeySignature();
    }
}
