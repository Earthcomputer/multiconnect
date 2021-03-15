package net.earthcomputer.multiconnect.protocols.generic;

import com.google.common.collect.ImmutableSet;
import net.earthcomputer.multiconnect.api.ICustomPayloadListener;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.CustomPayloadEvent;
import net.earthcomputer.multiconnect.protocols.v1_12_2.CustomPayloadC2SPacket_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_13_2.Protocol_1_13_2;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class CustomPayloadHandler {
    public static final Identifier DROP_ID = new Identifier("multiconnect", "drop");
    public static final Set<Identifier> VANILLA_CLIENTBOUND_CHANNELS;

    private static final List<ICustomPayloadListener<Identifier>> clientboundIdentifierCustomPayloadListeners = new CopyOnWriteArrayList<>();
    private static final List<ICustomPayloadListener<String>> clientboundStringCustomPayloadListeners = new CopyOnWriteArrayList<>();
    private static final List<ICustomPayloadListener<Identifier>> serverboundIdentifierCustomPayloadListeners = new CopyOnWriteArrayList<>();
    private static final List<ICustomPayloadListener<String>> serverboundStringCustomPayloadListeners = new CopyOnWriteArrayList<>();

    private static final Map<Identifier, String> clientboundStringCustomPayloadChannels = new WeakHashMap<>();
    private static volatile int clientboundStringCustomPayloadId = 0;

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

    public static void handleServerboundCustomPayload(ClientPlayNetworkHandler networkHandler, ICustomPayloadC2SPacket packet) {
        CustomPayloadEvent<Identifier> event = new CustomPayloadEvent<>(ConnectionInfo.protocolVersion, packet.multiconnect_getChannel(), packet.multiconnect_getData(), networkHandler);
        serverboundIdentifierCustomPayloadListeners.forEach(listener -> listener.onCustomPayload(event));
    }

    public static void handleServerboundCustomPayload(ClientPlayNetworkHandler networkHandler, CustomPayloadC2SPacket_1_12_2 packet) {
        CustomPayloadEvent<String> event = new CustomPayloadEvent<>(ConnectionInfo.protocolVersion, packet.getChannel(), packet.getData(), networkHandler);
        serverboundStringCustomPayloadListeners.forEach(listener -> listener.onCustomPayload(event));
    }

    public static void handleClientboundCustomPayload(ClientPlayNetworkHandler networkHandler, CustomPayloadS2CPacket packet) {
        String str;
        synchronized (clientboundStringCustomPayloadChannels) {
            str = clientboundStringCustomPayloadChannels.remove(packet.getChannel());
        }
        if (str != null) {
            handleClientboundStringCustomPayload(networkHandler, str, packet.getData());
        } else {
            handleClientboundIdentifierCustomPayload(networkHandler, packet);
        }
    }

    private static void handleClientboundIdentifierCustomPayload(ClientPlayNetworkHandler networkHandler, CustomPayloadS2CPacket packet) {
        CustomPayloadEvent<Identifier> event = new CustomPayloadEvent<>(ConnectionInfo.protocolVersion, packet.getChannel(), packet.getData(), networkHandler);
        clientboundIdentifierCustomPayloadListeners.forEach(listener -> listener.onCustomPayload(event));
    }

    private static void handleClientboundStringCustomPayload(ClientPlayNetworkHandler networkHandler, String channel, PacketByteBuf data) {
        CustomPayloadEvent<String> event = new CustomPayloadEvent<>(ConnectionInfo.protocolVersion, channel, data, networkHandler);
        clientboundStringCustomPayloadListeners.forEach(listener -> listener.onCustomPayload(event));
    }

    public static Identifier getClientboundIdentifierForStringCustomPayload(String channel) {
        synchronized (clientboundStringCustomPayloadChannels) {
            Identifier id = new Identifier("multiconnect", "generated_" + Integer.toUnsignedString(clientboundStringCustomPayloadId++));
            clientboundStringCustomPayloadChannels.put(id, channel);
            return id;
        }
    }

    static {
        ImmutableSet.Builder<Identifier> vanillaChannels = ImmutableSet.builder();

        // existing vanilla ones
        for (Field field : CustomPayloadS2CPacket.class.getDeclaredFields()) {
            if ((field.getModifiers() & (Modifier.STATIC | Modifier.FINAL)) == (Modifier.STATIC | Modifier.FINAL)) {
                if (field.getType() == Identifier.class) {
                    field.setAccessible(true);
                    try {
                        vanillaChannels.add((Identifier) field.get(null));
                    } catch (ReflectiveOperationException e) {
                        throw new AssertionError(e);
                    }
                }
            }
        }

        // removed vanilla ones, from when they actually used to custom payload for stuff
        vanillaChannels.add(Protocol_1_13_2.CUSTOM_PAYLOAD_OPEN_BOOK);
        vanillaChannels.add(Protocol_1_13_2.CUSTOM_PAYLOAD_TRADE_LIST);

        VANILLA_CLIENTBOUND_CHANNELS = vanillaChannels.build();
    }
}
