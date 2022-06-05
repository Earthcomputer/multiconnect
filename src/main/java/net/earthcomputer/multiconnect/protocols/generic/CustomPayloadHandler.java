package net.earthcomputer.multiconnect.protocols.generic;

import io.netty.buffer.Unpooled;
import net.earthcomputer.multiconnect.api.ICustomPayloadListener;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.api.ThreadSafe;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.CustomPayloadEvent;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.packets.latest.CPacketCustomPayload_Latest;
import net.earthcomputer.multiconnect.packets.v1_12_2.CPacketCustomPayload_1_12_2;
import net.minecraft.SharedConstants;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CustomPayloadHandler {
    public static final Key<Boolean> IS_FORCE_SENT = Key.create("isForceSent", false);

    private static final List<ICustomPayloadListener<Identifier>> clientboundIdentifierCustomPayloadListeners = new CopyOnWriteArrayList<>();
    private static final List<ICustomPayloadListener<String>> clientboundStringCustomPayloadListeners = new CopyOnWriteArrayList<>();
    private static final List<ICustomPayloadListener<Identifier>> serverboundIdentifierCustomPayloadListeners = new CopyOnWriteArrayList<>();
    private static final List<ICustomPayloadListener<String>> serverboundStringCustomPayloadListeners = new CopyOnWriteArrayList<>();

    public static void addClientboundIdentifierCustomPayloadListener(ICustomPayloadListener<Identifier> listener) {
        clientboundIdentifierCustomPayloadListeners.add(listener);
    }

    public static void removeClientboundIdentifierCustomPayloadListener(ICustomPayloadListener<Identifier> listener) {
        clientboundIdentifierCustomPayloadListeners.remove(listener);
    }

    public static void addClientboundStringCustomPayloadListener(ICustomPayloadListener<String> listener) {
        clientboundStringCustomPayloadListeners.add(listener);
    }

    public static void removeClientboundStringCustomPayloadListener(ICustomPayloadListener<String> listener) {
        clientboundStringCustomPayloadListeners.remove(listener);
    }

    public static void addServerboundIdentifierCustomPayloadListener(ICustomPayloadListener<Identifier> listener) {
        serverboundIdentifierCustomPayloadListeners.add(listener);
    }

    public static void removeServerboundIdentifierCustomPayloadListener(ICustomPayloadListener<Identifier> listener) {
        serverboundIdentifierCustomPayloadListeners.remove(listener);
    }

    public static void addServerboundStringCustomPayloadListener(ICustomPayloadListener<String> listener) {
        serverboundStringCustomPayloadListeners.add(listener);
    }

    public static void removeServerboundStringCustomPayloadListener(ICustomPayloadListener<String> listener) {
        serverboundStringCustomPayloadListeners.remove(listener);
    }

    public static void forceSendIdentifierCustomPayload(ClientPlayNetworkHandler networkHandler, Identifier channel, PacketByteBuf data) {
        var packet = new CPacketCustomPayload_Latest.Other();
        packet.channel = channel;
        packet.data = new byte[data.readableBytes()];
        data.readBytes(packet.data);
        PacketSystem.sendToServer(networkHandler, SharedConstants.getProtocolVersion(), packet, userData -> {
            userData.put(IS_FORCE_SENT, true);
        });
    }

    public static void forceSendStringCustomPayload(ClientPlayNetworkHandler networkHandler, String channel, PacketByteBuf data) {
        var packet = new CPacketCustomPayload_1_12_2.Other();
        packet.channel = channel;
        packet.data = new byte[data.readableBytes()];
        data.readBytes(packet.data);
        PacketSystem.sendToServer(networkHandler, Protocols.V1_12_2, packet, userData -> {
            userData.put(IS_FORCE_SENT, true);
        });
    }

    @ThreadSafe
    public static void handleServerboundCustomPayload(ClientPlayNetworkHandler networkHandler, Identifier channel, byte[] data) {
        var event = new CustomPayloadEvent<>(
                ConnectionInfo.protocolVersion,
                channel,
                new PacketByteBuf(Unpooled.wrappedBuffer(data)),
                networkHandler
        );
        serverboundIdentifierCustomPayloadListeners.forEach(listener -> listener.onCustomPayload(event));
    }

    @ThreadSafe
    public static void handleServerboundCustomPayload(ClientPlayNetworkHandler networkHandler, String channel, byte[] data) {
        var event = new CustomPayloadEvent<>(
                ConnectionInfo.protocolVersion,
                channel,
                new PacketByteBuf(Unpooled.wrappedBuffer(data)),
                networkHandler
        );
        serverboundStringCustomPayloadListeners.forEach(listener -> listener.onCustomPayload(event));
    }

    @ThreadSafe
    public static void handleClientboundIdentifierCustomPayload(ClientPlayNetworkHandler networkHandler, Identifier channel, byte[] data) {
        var event = new CustomPayloadEvent<>(
                ConnectionInfo.protocolVersion,
                channel,
                new PacketByteBuf(Unpooled.wrappedBuffer(data)),
                networkHandler
        );
        clientboundIdentifierCustomPayloadListeners.forEach(listener -> listener.onCustomPayload(event));
    }

    @ThreadSafe
    public static void handleClientboundStringCustomPayload(ClientPlayNetworkHandler networkHandler, String channel, byte[] data) {
        var event = new CustomPayloadEvent<>(
                ConnectionInfo.protocolVersion,
                channel,
                new PacketByteBuf(Unpooled.wrappedBuffer(data)),
                networkHandler
        );
        clientboundStringCustomPayloadListeners.forEach(listener -> listener.onCustomPayload(event));
    }
}
