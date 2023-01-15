package net.earthcomputer.multiconnect.impl;

import net.earthcomputer.multiconnect.api.*;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.generic.CustomPayloadHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class APIImpl extends MultiConnectAPI {
    @Override
    public int getProtocolVersion() {
        return ConnectionInfo.protocolVersion;
    }

    @Override
    public IProtocol byProtocolVersion(int version) {
        return ProtocolRegistry.get(version);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<IProtocol> getSupportedProtocols() {
        return (List<IProtocol>) (List<?>) ProtocolRegistry.getProtocols();
    }

    @Override
    public CustomProtocolBuilder createCustomProtocol(int version, String name, int dataVersion) {
        return new CustomProtocolImpl(version, name, dataVersion);
    }

    @Override
    public <T> boolean doesServerKnow(Registry<T> registry, T value) {
        return doesServerKnow(registry, registry.getResourceKey(value).orElseThrow(() -> new IllegalArgumentException("Called doesServerKnow on unregistered registry entry")));
    }

    @Override
    public <T> boolean doesServerKnow(Registry<T> registry, ResourceKey<T> key) {
        return Multiconnect.translator.doesServerKnow(registry.key().location().toString(), key.location().toString());
    }

    //region deprecated stuff

    @Deprecated
    @Override
    public void addClientboundIdentifierCustomPayloadListener(ICustomPayloadListener<ResourceLocation> listener) {
        CustomPayloadHandler.addClientboundIdentifierCustomPayloadListener(listener);
    }

    @Deprecated
    @Override
    public void removeClientboundIdentifierCustomPayloadListener(ICustomPayloadListener<ResourceLocation> listener) {
        CustomPayloadHandler.removeClientboundIdentifierCustomPayloadListener(listener);
    }

    @Deprecated
    @Override
    public void addClientboundStringCustomPayloadListener(ICustomPayloadListener<String> listener) {
        CustomPayloadHandler.addClientboundStringCustomPayloadListener(listener);
    }

    @Deprecated
    @Override
    public void removeClientboundStringCustomPayloadListener(ICustomPayloadListener<String> listener) {
        CustomPayloadHandler.removeClientboundStringCustomPayloadListener(listener);
    }

    @Deprecated
    @Contract("null, _, _ -> fail")
    @Override
    public void forceSendCustomPayload(@Nullable ClientPacketListener connection, ResourceLocation channel, FriendlyByteBuf data) {
        if (connection == null) {
            throw new IllegalStateException("Trying to send custom payload when not in-game");
        }
        CustomPayloadHandler.forceSendIdentifierCustomPayload(connection, channel, data);
    }

    @Deprecated
    @Override
    public void forceSendCustomPayload(ResourceLocation channel, FriendlyByteBuf data) {
        forceSendCustomPayload(Minecraft.getInstance().getConnection(), channel, data);
    }

    @Deprecated
    @Contract("null, _, _ -> fail")
    @Override
    public void forceSendStringCustomPayload(@Nullable ClientPacketListener connection, String channel, FriendlyByteBuf data) {
        if (connection == null) {
            throw new IllegalStateException("Trying to send custom payload when not in-game");
        }
        if (ConnectionInfo.protocolVersion > Protocols.V1_12_2) {
            throw new IllegalStateException("Trying to send string custom payload to " + ProtocolRegistry.get(ConnectionInfo.protocolVersion).getName() + " server");
        }
        CustomPayloadHandler.forceSendStringCustomPayload(connection, channel, data);
    }

    @Override
    public void forceSendStringCustomPayload(String channel, FriendlyByteBuf data) {
        forceSendStringCustomPayload(Minecraft.getInstance().getConnection(), channel, data);
    }

    @Deprecated
    @Override
    public void addServerboundIdentifierCustomPayloadListener(ICustomPayloadListener<ResourceLocation> listener) {
        CustomPayloadHandler.addServerboundIdentifierCustomPayloadListener(listener);
    }

    @Deprecated
    @Override
    public void removeServerboundIdentifierCustomPayloadListener(ICustomPayloadListener<ResourceLocation> listener) {
        CustomPayloadHandler.removeServerboundIdentifierCustomPayloadListener(listener);
    }

    @Deprecated
    @Override
    public void addServerboundStringCustomPayloadListener(ICustomPayloadListener<String> listener) {
        CustomPayloadHandler.addServerboundStringCustomPayloadListener(listener);
    }

    @Deprecated
    @Override
    public void removeServerboundStringCustomPayloadListener(ICustomPayloadListener<String> listener) {
        CustomPayloadHandler.removeServerboundStringCustomPayloadListener(listener);
    }

    @Deprecated
    @Override
    public void addIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener) {
        addClientboundIdentifierCustomPayloadListener(new IdentifierCustomPayloadListenerProxy(listener));
    }

    @Deprecated
    @Override
    public void removeIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener) {
        removeClientboundIdentifierCustomPayloadListener(new IdentifierCustomPayloadListenerProxy(listener));
    }

    @Deprecated
    @Override
    public void addStringCustomPayloadListener(IStringCustomPayloadListener listener) {
        addClientboundStringCustomPayloadListener(new StringCustomPayloadListenerProxy(listener));
    }

    @Deprecated
    @Override
    public void removeStringCustomPayloadListener(IStringCustomPayloadListener listener) {
        removeClientboundStringCustomPayloadListener(new StringCustomPayloadListenerProxy(listener));
    }

    @Deprecated
    @Override
    public void addServerboundIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener) {
        addServerboundIdentifierCustomPayloadListener(new IdentifierCustomPayloadListenerProxy(listener));
    }

    @Deprecated
    @Override
    public void removeServerboundIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener) {
        removeServerboundIdentifierCustomPayloadListener(new IdentifierCustomPayloadListenerProxy(listener));
    }

    @Deprecated
    @Override
    public void addServerboundStringCustomPayloadListener(IStringCustomPayloadListener listener) {
        addServerboundStringCustomPayloadListener(new StringCustomPayloadListenerProxy(listener));
    }

    @Deprecated
    @Override
    public void removeServerboundStringCustomPayloadListener(IStringCustomPayloadListener listener) {
        removeServerboundStringCustomPayloadListener(new StringCustomPayloadListenerProxy(listener));
    }

    @Deprecated
    private record IdentifierCustomPayloadListenerProxy(
            IIdentifierCustomPayloadListener delegate
    ) implements ICustomPayloadListener<ResourceLocation> {
        @Override
        public void onCustomPayload(ICustomPayloadEvent<ResourceLocation> event) {
            delegate.onCustomPayload(event.getProtocol(), event.getChannel(), event.getData());
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof IdentifierCustomPayloadListenerProxy && delegate.equals(((IdentifierCustomPayloadListenerProxy) obj).delegate);
        }
    }

    @Deprecated
    private record StringCustomPayloadListenerProxy(
            IStringCustomPayloadListener delegate
    ) implements ICustomPayloadListener<String> {
        @Override
        public void onCustomPayload(ICustomPayloadEvent<String> event) {
            delegate.onCustomPayload(event.getProtocol(), event.getChannel(), event.getData());
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof StringCustomPayloadListenerProxy && delegate.equals(((StringCustomPayloadListenerProxy) obj).delegate);
        }
    }

    //endregion
}
