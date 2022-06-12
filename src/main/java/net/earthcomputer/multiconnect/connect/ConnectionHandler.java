package net.earthcomputer.multiconnect.connect;

import com.mojang.logging.LogUtils;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.mixin.connect.HandshakePacketAccessor;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.Locale;

public class ConnectionHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static boolean preConnect(InetSocketAddress addr, ServerAddress serverAddress, String addressField) {
        // Hypixel has their own closed-source connection proxy and closed-source anti-cheat.
        // Users were getting banned for odd reasons. Their maps are designed to have fair play between clients on any
        // version, so we force the current protocol version here to disable any kind of bridge, in the hope that users
        // don't get banned because they are using multiconnect.
        String testIp = normalizeAddress(addr.getHostName()).split(":")[0].toLowerCase(Locale.ROOT);
        if (testIp.endsWith(".")) {
            testIp = testIp.substring(0, testIp.length() - 1);
        }
        if (testIp.equals("hypixel.net") || testIp.endsWith(".hypixel.net")) {
            if (SharedConstants.getGameVersion().isStable()) {
                ConnectionInfo.protocolVersion = SharedConstants.getGameVersion().getProtocolVersion();
            } else {
                ConnectionInfo.protocolVersion = ConnectionMode.protocolValues()[1].getValue();
            }
            LOGGER.info("Hypixel detected, protocol version forced to " + ConnectionInfo.protocolVersion + " (" + ConnectionMode.byValue(ConnectionInfo.protocolVersion).getName() + ")");
            return true;
        }

        if (addressField != null) {
            int forcedVersion = ServersExt.getInstance().getForcedProtocol(addressField);
            if (forcedVersion != ConnectionMode.AUTO.getValue()) {
                ConnectionInfo.protocolVersion = forcedVersion;
                LOGGER.info("Protocol version forced to " + ConnectionInfo.protocolVersion + " (" + ConnectionMode.byValue(forcedVersion).getName() + ")");
                return true;
            }
        }

        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (!(screen instanceof ConnectScreen))
            return true;
        IConnectScreen connectScreen = (IConnectScreen) screen;

        ClientConnection connection = ClientConnection.connect(addr, false);
        connectScreen.multiconnect_setVersionRequestConnection(connection);
        GetProtocolPacketListener listener = new GetProtocolPacketListener(connection);
        connection.setPacketListener(listener);

        HandshakeC2SPacket handshake  = new HandshakeC2SPacket(serverAddress.getAddress(), serverAddress.getPort(), NetworkState.STATUS);
        //noinspection ConstantConditions
        ((HandshakePacketAccessor) handshake).setProtocolVersion(-1);
        connection.send(handshake);
        connection.send(new QueryRequestC2SPacket());

        while (!listener.hasCompleted()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (connectScreen.isConnectingCancelled()) {
                connection.disconnect(Text.translatable("multiplayer.disconnected"));
                return false;
            }
        }

        if (listener.hasFailed()) {
            MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().setScreen(new DisconnectedScreen(connectScreen.getParent(), ScreenTexts.CONNECT_FAILED, Text.literal("Failed to request server protocol version"))));
        }

        connectScreen.multiconnect_setVersionRequestConnection(null);

        if (listener.hasFailed()) {
            return false;
        }

        int protocol = listener.getProtocol();
        if (ConnectionMode.isSupportedProtocol(protocol)) {
            LOGGER.info("Discovered server protocol: " + protocol + " (" + ConnectionMode.byValue(protocol).getName() + ")");
            ConnectionInfo.protocolVersion = protocol;
        } else {
            LOGGER.info("Discovered server protocol: " + protocol + " (unsupported), " +
                    "falling back to " + SharedConstants.getGameVersion().getProtocolVersion() + " (" + SharedConstants.getGameVersion().getName() + ")");
            ConnectionInfo.protocolVersion = SharedConstants.getGameVersion().getProtocolVersion();
        }

        return true;
    }

    public static void onSendHandshake(ClientConnection connect, Packet<?> handshakePacket) {
        if (ConnectionMode.isSupportedProtocol(ConnectionInfo.protocolVersion)) {
            ((HandshakePacketAccessor) handshakePacket).setProtocolVersion(ConnectionInfo.protocolVersion);
            ConnectionInfo.protocol = ProtocolRegistry.get(ConnectionInfo.protocolVersion);
            ConnectionInfo.protocol.setup(false);
        }
    }

    public static String normalizeAddress(String addressStr) {
        String[] addressAndPort = addressStr.split(":");

        if (addressStr.startsWith("[")) {
            int closeIndex = addressStr.indexOf(']');
            if (closeIndex >= 0) {
                String addressPart = addressStr.substring(1, closeIndex);
                String portPart = addressStr.substring(closeIndex + 1);
                if (portPart.startsWith(":")) {
                    addressAndPort = new String[] { addressPart, portPart.substring(1) };
                } else {
                    addressAndPort = new String[] { addressPart };
                }
            }
        }

        String address = addressAndPort[0];
        int port = 25565;
        if (addressAndPort.length == 2) {
            try {
                port = Integer.parseInt(addressAndPort[1]);
            } catch (NumberFormatException ignore) {
            }
        }

        if (address.contains(":")) {
            return "[" + address + "]:" + port;
        } else {
            return address + ":" + port;
        }
    }
}
