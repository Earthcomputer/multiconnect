package net.earthcomputer.multiconnect.protocols.generic;

import com.google.common.collect.ImmutableSet;
import net.earthcomputer.multiconnect.api.IIdentifierCustomPayloadListener;
import net.earthcomputer.multiconnect.api.IStringCustomPayloadListener;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
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
    public static final Set<Identifier> VANILLA_CHANNELS;

    private static List<IIdentifierCustomPayloadListener> identifierCustomPayloadListeners = new CopyOnWriteArrayList<>();
    private static List<IStringCustomPayloadListener> stringCustomPayloadListeners = new CopyOnWriteArrayList<>();

    private static final Map<Identifier, String> stringCustomPayloadChannels = new WeakHashMap<>();
    private static volatile int stringCustomPayloadId = 0;

    public static void addIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener) {
        identifierCustomPayloadListeners.add(listener);
    }

    public static void removeIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener) {
        identifierCustomPayloadListeners.remove(listener);
    }

    public static void addStringCustomPayloadListener(IStringCustomPayloadListener listener) {
        stringCustomPayloadListeners.add(listener);
    }

    public static void removeStringCustomPayloadListener(IStringCustomPayloadListener listener) {
        stringCustomPayloadListeners.remove(listener);
    }

    public static void handleCustomPayload(CustomPayloadS2CPacket packet) {
        String str;
        synchronized (stringCustomPayloadChannels) {
            str = stringCustomPayloadChannels.remove(packet.getChannel());
        }
        if (str != null) {
            handleStringCustomPayload(str, packet.getData());
        } else {
            handleIdentifierCustomPayload(packet);
        }
    }

    private static void handleIdentifierCustomPayload(CustomPayloadS2CPacket packet) {
        identifierCustomPayloadListeners.forEach(listener -> listener.onCustomPayload(ConnectionInfo.protocolVersion, packet.getChannel(), packet.getData()));
    }

    private static void handleStringCustomPayload(String channel, PacketByteBuf data) {
        stringCustomPayloadListeners.forEach(listener -> listener.onCustomPayload(ConnectionInfo.protocolVersion, channel, data));
    }

    public static Identifier getIdentifierForStringCustomPayload(String channel) {
        synchronized (stringCustomPayloadChannels) {
            Identifier id = new Identifier("multiconnect", "generated_" + Integer.toUnsignedString(stringCustomPayloadId++));
            stringCustomPayloadChannels.put(id, channel);
            return id;
        }
    }

    static {
        ImmutableSet.Builder<Identifier> vanillaChannels = ImmutableSet.builder();
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
        VANILLA_CHANNELS = vanillaChannels.build();
    }
}
