package net.earthcomputer.multiconnect.mixin.debug;

import net.earthcomputer.multiconnect.debug.PacketReplay;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientHandshakePacketListenerImpl.class)
public class ClientHandshakePacketListenerImplMixin {
    @Inject(method = "authenticateServer", at = @At("HEAD"), cancellable = true)
    private void dontAuthenticate(CallbackInfoReturnable<Component> ci) {
        if (PacketReplay.isReplaying()) {
            ci.setReturnValue(null);
        }
    }
}
