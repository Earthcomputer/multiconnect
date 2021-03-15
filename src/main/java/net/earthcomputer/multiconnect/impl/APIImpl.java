package net.earthcomputer.multiconnect.impl;

import net.earthcomputer.multiconnect.api.*;
import net.earthcomputer.multiconnect.connect.ConnectionMode;
import net.earthcomputer.multiconnect.protocols.generic.CustomPayloadHandler;
import net.earthcomputer.multiconnect.protocols.generic.DefaultRegistries;
import net.earthcomputer.multiconnect.protocols.generic.ICustomPayloadC2SPacket;
import net.earthcomputer.multiconnect.protocols.generic.ISimpleRegistry;
import net.earthcomputer.multiconnect.protocols.v1_12_2.CustomPayloadC2SPacket_1_12_2;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
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
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(channel, data);
        //noinspection ConstantConditions
        ((ICustomPayloadC2SPacket) packet).multiconnect_unblock();
        networkHandler.sendPacket(packet);
    }

    @Override
    public void forceSendStringCustomPayload(ClientPlayNetworkHandler networkHandler, String channel, PacketByteBuf data) {
        if (networkHandler == null) {
            throw new IllegalStateException("Trying to send custom payload when not in-game");
        }
        if (ConnectionInfo.protocolVersion > Protocols.V1_12_2) {
            throw new IllegalStateException("Trying to send string custom payload to " + ConnectionMode.byValue(ConnectionInfo.protocolVersion).getName() + " server");
        }
        CustomPayloadC2SPacket_1_12_2 packet = new CustomPayloadC2SPacket_1_12_2(channel, data);
        packet.unblock();
        networkHandler.sendPacket(packet);
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
        return registry.getKey(value).map(key -> doesServerKnow(registry, key)).orElse(false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> boolean doesServerKnow(Registry<T> registry, RegistryKey<T> key) {
        if (!DefaultRegistries.DEFAULT_REGISTRIES.containsKey(registry)) {
            return super.doesServerKnow(registry, key);
        }
        return ((ISimpleRegistry<T>) registry).getRealEntries().contains(key);
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
    private static final class IdentifierCustomPayloadListenerProxy implements ICustomPayloadListener<Identifier> {
        private final IIdentifierCustomPayloadListener delegate;

        private IdentifierCustomPayloadListenerProxy(IIdentifierCustomPayloadListener delegate) {
            this.delegate = delegate;
        }

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
    private static final class StringCustomPayloadListenerProxy implements ICustomPayloadListener<String> {
        private final IStringCustomPayloadListener delegate;

        private StringCustomPayloadListenerProxy(IStringCustomPayloadListener delegate) {
            this.delegate = delegate;
        }

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
