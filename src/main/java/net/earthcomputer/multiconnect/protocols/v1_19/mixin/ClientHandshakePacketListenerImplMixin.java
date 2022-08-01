package net.earthcomputer.multiconnect.protocols.v1_19.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.util.Signer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// TODO: fix login packets on the network layer
@Mixin(ClientHandshakePacketListenerImpl.class)
public class ClientHandshakePacketListenerImplMixin {
    @ModifyVariable(method = "handleHello", at = @At("STORE"))
    private Signer modifySigner(Signer signer) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_19) {
            return null;
        }
        return signer;
    }
}
