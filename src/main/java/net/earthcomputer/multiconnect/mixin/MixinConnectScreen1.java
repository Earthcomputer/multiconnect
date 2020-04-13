package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.ConnectionMode;
import net.earthcomputer.multiconnect.impl.*;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.ProtocolType;
import net.minecraft.network.handshake.client.CHandshakePacket;
import net.minecraft.network.status.client.CPingPacket;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Mixin(targets = "net.minecraft.client.gui.screen.ConnectingScreen$1")
public class MixinConnectScreen1 {

    @Inject(method = "run()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkManager;createNetworkManagerAndConnect(Ljava/net/InetAddress;IZ)Lnet/minecraft/network/NetworkManager;"), cancellable = true)
    public void beforeConnect(CallbackInfo ci) throws UnknownHostException {
        String address;
        ServerData serverInfo = Minecraft.getInstance().getCurrentServerData();
        if (serverInfo != null) {
            address = serverInfo.serverIP;
        } else if (ConnectionInfo.port == 25565) {
            address = ConnectionInfo.ip;
        } else {
            address = ConnectionInfo.ip + ":" + ConnectionInfo.port;
        }
        int forcedVersion = ServersExt.getInstance().getForcedProtocol(address);
        forcedVersion = forcedVersion == ConnectionMode.AUTO.getValue() ? ConnectionInfo.globalForcedProtocolVersion.getValue() : forcedVersion;

        if (forcedVersion != ConnectionMode.AUTO.getValue()) {
            ConnectionInfo.protocolVersion = forcedVersion;
            LogManager.getLogger("multiconnect").info("Protocol version forced to " + ConnectionInfo.protocolVersion + " (" + ConnectionMode.byValue(forcedVersion).getName() + ")");
            return;
        }

        Screen screen = Minecraft.getInstance().currentScreen;
        if (!(screen instanceof ConnectingScreen))
            return;
        IConnectScreen connectScreen = (IConnectScreen) screen;

        NetworkManager connection = NetworkManager.createNetworkManagerAndConnect(InetAddress.getByName(ConnectionInfo.ip), ConnectionInfo.port, false);
        connectScreen.multiconnect_setVersionRequestConnection(connection);
        GetProtocolPacketListener listener = new GetProtocolPacketListener(connection);
        connection.setNetHandler(listener);

        CHandshakePacket handshake  = new CHandshakePacket(ConnectionInfo.ip, ConnectionInfo.port, ProtocolType.STATUS);
        //noinspection ConstantConditions
        ((HandshakePacketAccessor) handshake).setProtocolVersion(-1);
        connection.sendPacket(handshake);
        connection.sendPacket(new CPingPacket());

        while (!listener.hasCompleted()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (connectScreen.isConnectingCancelled()) {
                connection.closeChannel(new TranslationTextComponent("multiplayer.disconnected"));
                ci.cancel();
                return;
            }
        }

        if (listener.hasFailed()) {
            Minecraft.getInstance().execute(() -> Minecraft.getInstance().displayGuiScreen(new DisconnectedScreen(connectScreen.getPreviousGuiScreen(), "connect.failed", new StringTextComponent("Failed to request server protocol version"))));
            ci.cancel();
        }

        connectScreen.multiconnect_setVersionRequestConnection(null);

        if (ConnectionMode.isSupportedProtocol(ConnectionInfo.protocolVersion)) {
            LogManager.getLogger("multiconnect").info("Discovered server protocol: " + ConnectionInfo.protocolVersion + " (" + ConnectionMode.byValue(ConnectionInfo.protocolVersion).getName() + ")");
        } else {
            LogManager.getLogger("multiconnect").info("Discovered server protocol: " + ConnectionInfo.protocolVersion + " (unsupported), " +
                    "falling back to " + SharedConstants.getVersion().getProtocolVersion() + " (" + SharedConstants.getVersion().getName() + ")");
            ConnectionInfo.protocolVersion = SharedConstants.getVersion().getProtocolVersion();
        }
    }

    @Redirect(method = "run()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkManager;sendPacket(Lnet/minecraft/network/IPacket;)V", ordinal = 0))
    public void sendHandshake(NetworkManager connect, IPacket<?> packet) {
        if (ConnectionMode.isSupportedProtocol(ConnectionInfo.protocolVersion)) {
            ((HandshakePacketAccessor) packet).setProtocolVersion(ConnectionInfo.protocolVersion);
            ConnectionInfo.protocol = ProtocolRegistry.get(ConnectionInfo.protocolVersion);
            ConnectionInfo.protocol.setup(false);
        }
        connect.sendPacket(packet);
    }

}
