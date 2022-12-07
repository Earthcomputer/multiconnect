package net.earthcomputer.multiconnect.connect;

import com.mojang.logging.LogUtils;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.mixin.connect.ClientIntentionPacketAccessor;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.status.ServerboundStatusRequestPacket;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.Locale;

public class ConnectionHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static boolean preConnect(InetSocketAddress addr, ServerAddress serverAddress, @Nullable String addressField) {
        // Hypixel has their own closed-source connection proxy and closed-source anti-cheat.
        // Users were getting banned for odd reasons. Their maps are designed to have fair play between clients on any
        // version, so we force the current protocol version here to disable any kind of bridge, in the hope that users
        // don't get banned because they are using multiconnect.
        String testIp = normalizeAddress(addr.getHostName()).split(":")[0].toLowerCase(Locale.ROOT);
        if (testIp.endsWith(".")) {
            testIp = testIp.substring(0, testIp.length() - 1);
        }
        if (testIp.equals("hypixel.net") || testIp.endsWith(".hypixel.net")) {
            if (SharedConstants.getCurrentVersion().isStable()) {
                ConnectionInfo.protocolVersion = SharedConstants.getCurrentVersion().getProtocolVersion();
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

        Screen screen = Minecraft.getInstance().screen;
        if (!(screen instanceof ConnectScreen))
            return true;
        IConnectScreen connectScreen = (IConnectScreen) screen;

        Connection connection = Connection.connectToServer(addr, false);
        connectScreen.multiconnect_setVersionRequestConnection(connection);
        GetProtocolPacketListener listener = new GetProtocolPacketListener(connection);
        connection.setListener(listener);

        ClientIntentionPacket intentionPacket = new ClientIntentionPacket(serverAddress.getHost(), serverAddress.getPort(), ConnectionProtocol.STATUS);
        //noinspection ConstantConditions
        ((ClientIntentionPacketAccessor) intentionPacket).setProtocolVersion(-1);
        connection.send(intentionPacket);
        connection.send(new ServerboundStatusRequestPacket());

        try {
            listener.await();
        } catch (InterruptedException e) {
            connection.disconnect(Component.translatable("multiplayer.disconnected"));
            return false;
        }

        if (listener.hasFailed()) {
            Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new DisconnectedScreen(connectScreen.getParent(), CommonComponents.CONNECT_FAILED, Component.literal("Failed to request server protocol version"))));
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
                    "falling back to " + SharedConstants.getCurrentVersion().getProtocolVersion() + " (" + SharedConstants.getCurrentVersion().getName() + ")");
            ConnectionInfo.protocolVersion = SharedConstants.getCurrentVersion().getProtocolVersion();
        }

        return true;
    }

    public static void onSendIntention() {
        if (ConnectionMode.isSupportedProtocol(ConnectionInfo.protocolVersion)) {
            ConnectionInfo.protocol = ProtocolRegistry.get(ConnectionInfo.protocolVersion);
            ConnectionInfo.protocol.setup();
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
