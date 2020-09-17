package net.earthcomputer.multiconnect.mixin.connect;

import net.earthcomputer.multiconnect.connect.ConnectionHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.net.InetAddress;

@Mixin(targets = "net.minecraft.client.gui.screen.ConnectScreen$1")
public class MixinConnectScreen1 {

    @SuppressWarnings("ShadowTarget")
    @Shadow
    private int field_2415; // port

    @Inject(method = "run()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;connect(Ljava/net/InetAddress;IZ)Lnet/minecraft/network/ClientConnection;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void beforeConnect(CallbackInfo ci, InetAddress address) {
        ServerInfo serverEntry = MinecraftClient.getInstance().getCurrentServerEntry();
        if (!ConnectionHandler.preConnect(address, field_2415, serverEntry == null ? null : serverEntry.address)) {
            ci.cancel();
        }
    }

    @Redirect(method = "run()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/Packet;)V", ordinal = 0))
    public void sendHandshake(ClientConnection connect, Packet<?> packet) {
        ConnectionHandler.onSendHandshake(connect, packet);
        connect.send(packet);
    }

}
