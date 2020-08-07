package net.earthcomputer.multiconnect.protocols.generic;

import com.google.common.collect.ImmutableSet;
import net.earthcomputer.multiconnect.api.IIdentifierCustomPayloadListener;
import net.earthcomputer.multiconnect.api.IStringCustomPayloadListener;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_12_2.CustomPayloadC2SPacket_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_13_2.Protocol_1_13_2;
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

    private static final List<IIdentifierCustomPayloadListener> clientboundIdentifierCustomPayloadListeners = new CopyOnWriteArrayList<>();
    private static final List<IStringCustomPayloadListener> clientboundStringCustomPayloadListeners = new CopyOnWriteArrayList<>();
    private static final List<IIdentifierCustomPayloadListener> serverboundIdentifierCustomPayloadListeners = new CopyOnWriteArrayList<>();
    private static final List<IStringCustomPayloadListener> serverboundStringCustomPayloadListeners = new CopyOnWriteArrayList<>();

    private static final Map<Identifier, String> clientboundStringCustomPayloadChannels = new WeakHashMap<>();
    private static volatile int clientboundStringCustomPayloadId = 0;

    public static void addClientboundIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener) {
        clientboundIdentifierCustomPayloadListeners.add(listener);
    }

    public static void removeClientboundIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener) {
        clientboundIdentifierCustomPayloadListeners.remove(listener);
    }

    public static void addClientboundStringCustomPayloadListener(IStringCustomPayloadListener listener) {
        clientboundStringCustomPayloadListeners.add(listener);
    }

    public static void removeClientboundStringCustomPayloadListener(IStringCustomPayloadListener listener) {
        clientboundStringCustomPayloadListeners.remove(listener);
    }

    public static void addServerboundIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener) {
        serverboundIdentifierCustomPayloadListeners.add(listener);
    }

    public static void removeServerboundIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener) {
        serverboundIdentifierCustomPayloadListeners.remove(listener);
    }

    public static void addServerboundStringCustomPayloadListener(IStringCustomPayloadListener listener) {
        serverboundStringCustomPayloadListeners.add(listener);
    }

    public static void removeServerboundStringCustomPayloadListener(IStringCustomPayloadListener listener) {
        serverboundStringCustomPayloadListeners.remove(listener);
    }

    public static void handleServerboundCustomPayload(ICustomPayloadC2SPacket packet) {
        serverboundIdentifierCustomPayloadListeners.forEach(listener -> listener.onCustomPayload(ConnectionInfo.protocolVersion, packet.multiconnect_getChannel(), packet.multiconnect_getData()));
    }

    public static void handleServerboundCustomPayload(CustomPayloadC2SPacket_1_12_2 packet) {
        serverboundStringCustomPayloadListeners.forEach(listener -> listener.onCustomPayload(ConnectionInfo.protocolVersion, packet.getChannel(), packet.getData()));
    }

    public static void handleClientboundCustomPayload(CustomPayloadS2CPacket packet) {
        String str;
        synchronized (clientboundStringCustomPayloadChannels) {
            str = clientboundStringCustomPayloadChannels.remove(packet.getChannel());
        }
        if (str != null) {
            handleClientboundStringCustomPayload(str, packet.getData());
        } else {
            handleClientboundIdentifierCustomPayload(packet);
        }
    }

    private static void handleClientboundIdentifierCustomPayload(CustomPayloadS2CPacket packet) {
        clientboundIdentifierCustomPayloadListeners.forEach(listener -> listener.onCustomPayload(ConnectionInfo.protocolVersion, packet.getChannel(), packet.getData()));
    }

    private static void handleClientboundStringCustomPayload(String channel, PacketByteBuf data) {
        clientboundStringCustomPayloadListeners.forEach(listener -> listener.onCustomPayload(ConnectionInfo.protocolVersion, channel, data));
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
