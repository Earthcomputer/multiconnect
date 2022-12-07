package net.earthcomputer.multiconnect.mixin.connect;

import net.earthcomputer.multiconnect.connect.ConnectionHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.net.InetSocketAddress;

@Mixin(targets = "net.minecraft.client.gui.screens.ConnectScreen$1")
public class ConnectScreen1Mixin {

    @Shadow(aliases = "field_33737", remap = false)
    @Final
    ServerAddress val$hostAndPort;

    @Inject(method = "run()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;connectToServer(Ljava/net/InetSocketAddress;Z)Lnet/minecraft/network/Connection;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void beforeConnect(CallbackInfo ci, InetSocketAddress address) {
        ServerData serverEntry = Minecraft.getInstance().getCurrentServer();
        if (!ConnectionHandler.preConnect(address, val$hostAndPort, serverEntry == null ? null : serverEntry.ip)) {
            ci.cancel();
        }
    }

    @Redirect(method = "run()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 0))
    public void sendHandshake(Connection connect, Packet<?> packet) {
        ConnectionHandler.onSendIntention();
        connect.send(packet);
    }

}
