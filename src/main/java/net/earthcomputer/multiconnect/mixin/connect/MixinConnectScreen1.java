package net.earthcomputer.multiconnect.mixin.connect;

import net.earthcomputer.multiconnect.connect.ConnectionHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.UnknownHostException;

@SuppressWarnings("UnresolvedMixinReference")
@Mixin(targets = "net.minecraft.client.gui.screen.ConnectScreen$1")
public class MixinConnectScreen1 {

    @Inject(method = "run()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;connect(Ljava/net/InetAddress;IZ)Lnet/minecraft/network/ClientConnection;"), cancellable = true)
    public void beforeConnect(CallbackInfo ci) throws UnknownHostException {
        if (!ConnectionHandler.preConnect()) {
            ci.cancel();
        }
    }

    @Redirect(method = "run()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/Packet;)V", ordinal = 0))
    public void sendHandshake(ClientConnection connect, Packet<?> packet) {
        ConnectionHandler.onSendHandshake(connect, packet);
        connect.send(packet);
    }

}
