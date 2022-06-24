package net.earthcomputer.multiconnect.protocols.v1_19.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.encryption.Signer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// TODO: fix login packets on the network layer
@Mixin(ClientLoginNetworkHandler.class)
public class MixinClientLoginNetworkHandler {
    @ModifyVariable(method = "onHello", at = @At("STORE"))
    private Signer modifySigner(Signer signer) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_19) {
            return null;
        }
        return signer;
    }
}
