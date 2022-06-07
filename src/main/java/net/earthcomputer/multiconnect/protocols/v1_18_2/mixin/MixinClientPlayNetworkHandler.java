package net.earthcomputer.multiconnect.protocols.v1_18_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.MulticonnectConfig;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {
    @Inject(method = "isSignatureValid", at = @At("HEAD"), cancellable = true)
    private void onIsSignatureValid(CallbackInfoReturnable<Boolean> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_18_2 && MulticonnectConfig.INSTANCE.allowOldUnsignedChat == Boolean.TRUE) {
            ci.setReturnValue(Boolean.TRUE);
        }
    }
}
