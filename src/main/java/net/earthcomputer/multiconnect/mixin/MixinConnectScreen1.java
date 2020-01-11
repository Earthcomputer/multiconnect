package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.api.EnumProtocol;
import net.earthcomputer.multiconnect.impl.*;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ServerInfo;
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

    @Inject(method = "run()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;connect(Ljava/net/InetAddress;IZ)Lnet/minecraft/network/ClientConnection;"), cancellable = true)
    public void beforeConnect(CallbackInfo ci) throws UnknownHostException {
        ServerInfo serverInfo = MinecraftClient.getInstance().getCurrentServerEntry();
        if (serverInfo != null) {
            EnumProtocol forcedVersion = ((IServerInfo) serverInfo).multiconnect_getForcedVersion();
            if (forcedVersion != EnumProtocol.AUTO) {
                ConnectionInfo.protocolVersion = forcedVersion.getValue();
                LogManager.getLogger("multiconnect").info("Protocol version forced to " + ConnectionInfo.protocolVersion + " (" + forcedVersion.getName() + ")");
                return;
            }
        }

        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (!(screen instanceof ConnectScreen))
            return;
        IConnectScreen connectScreen = (IConnectScreen) screen;

        ClientConnection connection = ClientConnection.connect(InetAddress.getByName(ConnectionInfo.ip), ConnectionInfo.port, false);
        connectScreen.multiconnect_setVersionRequestConnection(connection);
        GetProtocolPacketListener listener = new GetProtocolPacketListener(connection);
        connection.setPacketListener(listener);

        HandshakeC2SPacket handshake  = new HandshakeC2SPacket(ConnectionInfo.ip, ConnectionInfo.port, NetworkState.STATUS);
        //noinspection ConstantConditions
        ((IHandshakePacket) handshake).setVersion(-1);
        connection.send(handshake);
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

        connectScreen.multiconnect_setVersionRequestConnection(null);

        LogManager.getLogger("multiconnect").info("Discovered server protocol: " + ConnectionInfo.protocolVersion + " (" + EnumProtocol.byValue(ConnectionInfo.protocolVersion).getName() + ")");
    }

    @Redirect(method = "run()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/Packet;)V", ordinal = 0))
    public void sendHandshake(ClientConnection connect, Packet<?> packet) {
        if (ProtocolRegistry.isSupported(ConnectionInfo.protocolVersion)) {
            ((IHandshakePacket) packet).setVersion(ConnectionInfo.protocolVersion);
            ConnectionInfo.protocol = ProtocolRegistry.get(ConnectionInfo.protocolVersion);
            ConnectionInfo.protocol.setup(false);
        }
        connect.send(packet);
    }

}
