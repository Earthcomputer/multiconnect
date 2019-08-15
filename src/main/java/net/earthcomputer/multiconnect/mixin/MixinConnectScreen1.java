package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.ConnectionInfo;
import net.earthcomputer.multiconnect.GetProtocolPacketListener;
import net.earthcomputer.multiconnect.IConnectScreen;
import net.earthcomputer.multiconnect.IHandshakePacket;
import net.earthcomputer.multiconnect.protocol.Protocols;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.Packet;
import net.minecraft.server.network.packet.HandshakeC2SPacket;
import net.minecraft.server.network.packet.QueryRequestC2SPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Mixin(targets = "net.minecraft.client.gui.screen.ConnectScreen$1")
public class MixinConnectScreen1 {

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;connect(Ljava/net/InetAddress;IZ)Lnet/minecraft/network/ClientConnection;"), cancellable = true)
    public void beforeConnect(CallbackInfo ci) throws UnknownHostException {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (!(screen instanceof ConnectScreen))
            return;
        IConnectScreen connectScreen = (IConnectScreen) screen;

        ClientConnection connection = ClientConnection.connect(InetAddress.getByName(ConnectionInfo.ip), ConnectionInfo.port, false);
        GetProtocolPacketListener listener = new GetProtocolPacketListener(connection);
        connection.setPacketListener(listener);

        connection.send(new HandshakeC2SPacket(ConnectionInfo.ip, ConnectionInfo.port, NetworkState.STATUS));
        connection.send(new QueryRequestC2SPacket());

        while (!listener.hasCompleted()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (connectScreen.isConnectingCancelled()) {
                connection.disconnect(new TranslatableText("multiplayer.disconnected"));
                ci.cancel();
                return;
            }
        }

        if (listener.hasFailed()) {
            MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().openScreen(new DisconnectedScreen(connectScreen.getParent(), "connect.failed", new LiteralText("Failed to request server protocol version"))));
            ci.cancel();
        }

        LogManager.getLogger("multiconnect").info("Discovered server protocol: " + ConnectionInfo.protocolVersion);
    }

    @Redirect(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/Packet;)V", ordinal = 0))
    public void sendHandshake(ClientConnection connect, Packet<?> packet) {
        if (Protocols.isSupported(ConnectionInfo.protocolVersion)) {
            ((IHandshakePacket) packet).setVersion(ConnectionInfo.protocolVersion);
            ConnectionInfo.protocol = Protocols.get(ConnectionInfo.protocolVersion);
            ConnectionInfo.protocol.setup();
        }
    }

}
