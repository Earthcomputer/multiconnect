package net.earthcomputer.multiconnect.protocols.generic;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import io.netty.buffer.Unpooled;
import net.earthcomputer.multiconnect.api.ICustomPayloadListener;
import net.earthcomputer.multiconnect.api.ThreadSafe;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.CustomPayloadEvent;
import net.earthcomputer.multiconnect.impl.Multiconnect;
import net.earthcomputer.multiconnect.mixin.connect.ConnectionAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class CustomPayloadHandler {
    @Deprecated
    private static final List<ICustomPayloadListener<ResourceLocation>> clientboundIdentifierCustomPayloadListeners = new CopyOnWriteArrayList<>();
    @Deprecated
    private static final List<ICustomPayloadListener<String>> clientboundStringCustomPayloadListeners = new CopyOnWriteArrayList<>();
    @Deprecated
    private static final List<ICustomPayloadListener<ResourceLocation>> serverboundIdentifierCustomPayloadListeners = new CopyOnWriteArrayList<>();
    @Deprecated
    private static final List<ICustomPayloadListener<String>> serverboundStringCustomPayloadListeners = new CopyOnWriteArrayList<>();

    private static final Set<ResourceLocation> clientboundAllowedCustomPayloads;
    private static final Set<String> clientboundAllowedNamespaces;
    private static final Set<ResourceLocation> serverboundAllowedCustomPayloads;
    private static final Set<String> serverboundAllowedNamespaces;
    private static final BiMap<ResourceLocation, String> clientboundCustomPayloadNames112;
    private static final BiMap<ResourceLocation, String> serverboundCustomPayloadNames112;

    static {
        var clientboundBuilder = ImmutableSet.<ResourceLocation>builder();
        var clientboundNamespacesBuilder = ImmutableSet.<String>builder();
        var serverboundBuilder = ImmutableSet.<ResourceLocation>builder();
        var serverboundNamespacesBuilder = ImmutableSet.<String>builder();
        var clientbound112Builder = ImmutableBiMap.<ResourceLocation, String>builder();
        var serverbound112Builder = ImmutableBiMap.<ResourceLocation, String>builder();

        populateCustomPayloadFilters(clientboundBuilder, clientboundNamespacesBuilder, serverboundBuilder, serverboundNamespacesBuilder, clientbound112Builder, serverbound112Builder);

        clientboundAllowedCustomPayloads = clientboundBuilder.build();
        clientboundAllowedNamespaces = clientboundNamespacesBuilder.build();
        serverboundAllowedCustomPayloads = serverboundBuilder.build();
        serverboundAllowedNamespaces = serverboundNamespacesBuilder.build();
        clientboundCustomPayloadNames112 = clientbound112Builder.build();
        serverboundCustomPayloadNames112 = serverbound112Builder.build();
    }

    private static void populateCustomPayloadFilters(
        ImmutableSet.Builder<ResourceLocation> clientboundBuilder,
        ImmutableSet.Builder<String> clientboundNamespacesBuilder,
        ImmutableSet.Builder<ResourceLocation> serverboundBuilder,
        ImmutableSet.Builder<String> serverboundNamespacesBuilder,
        ImmutableBiMap.Builder<ResourceLocation, String> clientbound112Builder,
        ImmutableBiMap.Builder<ResourceLocation, String> serverbound112Builder
    ) {
        for (ModContainer modContainer : FabricLoader.getInstance().getAllMods()) {
            CustomValue multiconnect = modContainer.getMetadata().getCustomValue("multiconnect");
            if (multiconnect == null || multiconnect.getType() != CustomValue.CvType.OBJECT) {
                continue;
            }
            CustomValue customPayloads = multiconnect.getAsObject().get("custom_payloads");
            if (customPayloads == null || customPayloads.getType() != CustomValue.CvType.OBJECT) {
                continue;
            }

            loadAllowedCustomPayloads(customPayloads, "allowed_clientbound", clientboundBuilder, clientboundNamespacesBuilder);
            loadAllowedCustomPayloads(customPayloads, "allowed_serverbound", serverboundBuilder, serverboundNamespacesBuilder);
            load112Mappings(customPayloads, "clientbound_112_names", clientbound112Builder);
            load112Mappings(customPayloads, "serverbound_112_names", serverbound112Builder);
        }
    }

    private static void loadAllowedCustomPayloads(
        CustomValue customPayloads,
        String key,
        ImmutableSet.Builder<ResourceLocation> builder,
        ImmutableSet.Builder<String> namespacesBuilder
    ) {
        CustomValue allowed = customPayloads.getAsObject().get(key);
        if (allowed != null) {
            if (allowed.getType() == CustomValue.CvType.ARRAY) {
                for (CustomValue allowedStr : allowed.getAsArray()) {
                    if (allowedStr.getType() == CustomValue.CvType.STRING) {
                        loadAllowedCustomPayload(allowedStr.getAsString(), builder, namespacesBuilder);
                    }
                }
            } else if (allowed.getType() == CustomValue.CvType.STRING) {
                loadAllowedCustomPayload(allowed.getAsString(), builder, namespacesBuilder);
            }
        }
    }

    private static void loadAllowedCustomPayload(
        String value,
        ImmutableSet.Builder<ResourceLocation> builder,
        ImmutableSet.Builder<String> namespacesBuilder
    ) {
        if (value.endsWith(":*")) {
            namespacesBuilder.add(value.substring(0, value.length() - 2));
        } else {
            builder.add(new ResourceLocation(value));
        }
    }

    private static void load112Mappings(
        CustomValue customPayloads,
        String key,
        ImmutableBiMap.Builder<ResourceLocation, String> builder
    ) {
        CustomValue names = customPayloads.getAsObject().get(key);
        if (names != null && names.getType() == CustomValue.CvType.OBJECT) {
            for (var entry : names.getAsObject()) {
                if (entry.getValue().getType() == CustomValue.CvType.STRING) {
                    builder.put(new ResourceLocation(entry.getKey()), entry.getValue().getAsString());
                }
            }
        }
    }

    public static boolean allowClientboundCustomPayload(ResourceLocation channel) {
        return clientboundAllowedCustomPayloads.contains(channel) || clientboundAllowedNamespaces.contains(channel.getNamespace());
    }

    public static boolean allowServerboundCustomPayload(ResourceLocation channel) {
        return serverboundAllowedCustomPayloads.contains(channel) || serverboundAllowedNamespaces.contains(channel.getNamespace());
    }

    @Nullable
    public static ResourceLocation getClientboundChannel112(String channel) {
        ResourceLocation result = clientboundCustomPayloadNames112.inverse().get(channel);
        if (result != null) {
            return result;
        }
        return ResourceLocation.tryParse(channel);
    }

    public static String getServerboundChannel112(ResourceLocation channel) {
        String result = serverboundCustomPayloadNames112.get(channel);
        if (result != null) {
            return result;
        }
        return channel.toString();
    }

    @Deprecated
    public static void addClientboundIdentifierCustomPayloadListener(ICustomPayloadListener<ResourceLocation> listener) {
        clientboundIdentifierCustomPayloadListeners.add(listener);
    }

    @Deprecated
    public static void removeClientboundIdentifierCustomPayloadListener(ICustomPayloadListener<ResourceLocation> listener) {
        clientboundIdentifierCustomPayloadListeners.remove(listener);
    }

    @Deprecated
    public static void addClientboundStringCustomPayloadListener(ICustomPayloadListener<String> listener) {
        clientboundStringCustomPayloadListeners.add(listener);
    }

    @Deprecated
    public static void removeClientboundStringCustomPayloadListener(ICustomPayloadListener<String> listener) {
        clientboundStringCustomPayloadListeners.remove(listener);
    }

    @Deprecated
    public static void addServerboundIdentifierCustomPayloadListener(ICustomPayloadListener<ResourceLocation> listener) {
        serverboundIdentifierCustomPayloadListeners.add(listener);
    }

    @Deprecated
    public static void removeServerboundIdentifierCustomPayloadListener(ICustomPayloadListener<ResourceLocation> listener) {
        serverboundIdentifierCustomPayloadListeners.remove(listener);
    }

    @Deprecated
    public static void addServerboundStringCustomPayloadListener(ICustomPayloadListener<String> listener) {
        serverboundStringCustomPayloadListeners.add(listener);
    }

    @Deprecated
    public static void removeServerboundStringCustomPayloadListener(ICustomPayloadListener<String> listener) {
        serverboundStringCustomPayloadListeners.remove(listener);
    }

    @Deprecated
    public static void forceSendIdentifierCustomPayload(ClientPacketListener connection, ResourceLocation channel, FriendlyByteBuf data) {
        connection.send(new ServerboundCustomPayloadPacket(channel, data));
    }

    @Deprecated
    public static void forceSendStringCustomPayload(ClientPacketListener connection, String channel, FriendlyByteBuf data) {
        try {
            Multiconnect.translator.sendStringCustomPayload(((ConnectionAccessor) connection.getConnection()).getChannel(), channel, data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    @ThreadSafe
    public static void handleServerboundCustomPayload(ClientPacketListener connection, ResourceLocation channel, byte[] data) {
        var event = new CustomPayloadEvent<>(
                ConnectionInfo.protocolVersion,
                channel,
                new FriendlyByteBuf(Unpooled.wrappedBuffer(data)),
                connection
        );
        serverboundIdentifierCustomPayloadListeners.forEach(listener -> listener.onCustomPayload(event));
    }

    @Deprecated
    @ThreadSafe
    public static void handleServerboundCustomPayload(ClientPacketListener connection, String channel, byte[] data) {
        var event = new CustomPayloadEvent<>(
                ConnectionInfo.protocolVersion,
                channel,
                new FriendlyByteBuf(Unpooled.wrappedBuffer(data)),
                connection
        );
        serverboundStringCustomPayloadListeners.forEach(listener -> listener.onCustomPayload(event));
    }

    @Deprecated
    @ThreadSafe
    public static void handleClientboundIdentifierCustomPayload(ClientPacketListener connection, ResourceLocation channel, byte[] data) {
        var event = new CustomPayloadEvent<>(
                ConnectionInfo.protocolVersion,
                channel,
                new FriendlyByteBuf(Unpooled.wrappedBuffer(data)),
                connection
        );
        clientboundIdentifierCustomPayloadListeners.forEach(listener -> listener.onCustomPayload(event));
    }

    @Deprecated
    @ThreadSafe
    public static void handleClientboundStringCustomPayload(ClientPacketListener connection, String channel, byte[] data) {
        var event = new CustomPayloadEvent<>(
                ConnectionInfo.protocolVersion,
                channel,
                new FriendlyByteBuf(Unpooled.wrappedBuffer(data)),
                connection
        );
        clientboundStringCustomPayloadListeners.forEach(listener -> listener.onCustomPayload(event));
    }
}
