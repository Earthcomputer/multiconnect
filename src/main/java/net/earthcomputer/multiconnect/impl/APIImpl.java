package net.earthcomputer.multiconnect.impl;

import net.earthcomputer.multiconnect.api.*;
import net.earthcomputer.multiconnect.connect.ConnectionMode;
import net.earthcomputer.multiconnect.protocols.generic.CustomPayloadHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import java.util.Arrays;
import java.util.List;

public class APIImpl extends MultiConnectAPI {
    @Override
    public int getProtocolVersion() {
        return ConnectionInfo.protocolVersion;
    }

    @Override
    public IProtocol byProtocolVersion(int version) {
        ConnectionMode protocol = ConnectionMode.byValue(version);
        return protocol == ConnectionMode.AUTO ? null : protocol;
    }

    @Override
    public List<IProtocol> getSupportedProtocols() {
        return Arrays.asList(ConnectionMode.protocolValues());
    }

    @Override
    public void addClientboundIdentifierCustomPayloadListener(ICustomPayloadListener<Identifier> listener) {
        CustomPayloadHandler.addClientboundIdentifierCustomPayloadListener(listener);
    }

    @Override
    public void removeClientboundIdentifierCustomPayloadListener(ICustomPayloadListener<Identifier> listener) {
        CustomPayloadHandler.removeClientboundIdentifierCustomPayloadListener(listener);
    }

    @Override
    public void addClientboundStringCustomPayloadListener(ICustomPayloadListener<String> listener) {
        CustomPayloadHandler.addClientboundStringCustomPayloadListener(listener);
    }

    @Override
    public void removeClientboundStringCustomPayloadListener(ICustomPayloadListener<String> listener) {
        CustomPayloadHandler.removeClientboundStringCustomPayloadListener(listener);
    }

    @Override
    public void forceSendCustomPayload(ClientPlayNetworkHandler networkHandler, Identifier channel, PacketByteBuf data) {
        if (networkHandler == null) {
            throw new IllegalStateException("Trying to send custom payload when not in-game");
        }
        CustomPayloadHandler.forceSendIdentifierCustomPayload(networkHandler, channel, data);
    }

    @Override
    public void forceSendStringCustomPayload(ClientPlayNetworkHandler networkHandler, String channel, PacketByteBuf data) {
        if (networkHandler == null) {
            throw new IllegalStateException("Trying to send custom payload when not in-game");
        }
        if (ConnectionInfo.protocolVersion > Protocols.V1_12_2) {
            throw new IllegalStateException("Trying to send string custom payload to " + ConnectionMode.byValue(ConnectionInfo.protocolVersion).getName() + " server");
        }
        CustomPayloadHandler.forceSendStringCustomPayload(networkHandler, channel, data);
    }

    @Override
    public void addServerboundIdentifierCustomPayloadListener(ICustomPayloadListener<Identifier> listener) {
        CustomPayloadHandler.addServerboundIdentifierCustomPayloadListener(listener);
    }

    @Override
    public void removeServerboundIdentifierCustomPayloadListener(ICustomPayloadListener<Identifier> listener) {
        CustomPayloadHandler.removeServerboundIdentifierCustomPayloadListener(listener);
    }

    @Override
    public void addServerboundStringCustomPayloadListener(ICustomPayloadListener<String> listener) {
        CustomPayloadHandler.addServerboundStringCustomPayloadListener(listener);
    }

    @Override
    public void removeServerboundStringCustomPayloadListener(ICustomPayloadListener<String> listener) {
        CustomPayloadHandler.removeServerboundStringCustomPayloadListener(listener);
    }

    @Override
    public <T> boolean doesServerKnow(Registry<T> registry, T value) {
        return PacketSystem.doesServerKnow(registry, value);
    }

    @Override
    public <T> boolean doesServerKnow(Registry<T> registry, RegistryKey<T> key) {
        return PacketSystem.doesServerKnow(registry, key);
    }

    //region deprecated stuff

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
    ) implements ICustomPayloadListener<Identifier> {
        @Override
        public void onCustomPayload(ICustomPayloadEvent<Identifier> event) {
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
