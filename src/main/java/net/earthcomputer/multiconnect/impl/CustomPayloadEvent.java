package net.earthcomputer.multiconnect.impl;

import net.earthcomputer.multiconnect.api.ICustomPayloadEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

public final class CustomPayloadEvent<T> implements ICustomPayloadEvent<T> {
    private final int protocol;
    private final T channel;
    private final PacketByteBuf data;
    private final ClientPlayNetworkHandler networkHandler;

    public CustomPayloadEvent(int protocol, T channel, PacketByteBuf data, ClientPlayNetworkHandler networkHandler) {
        this.protocol = protocol;
        this.channel = channel;
        this.data = data;
        this.networkHandler = networkHandler;
    }

    @Override
    public int getProtocol() {
        return protocol;
    }

    @Override
    public T getChannel() {
        return channel;
    }

    @Override
    public PacketByteBuf getData() {
        return data;
    }

    @Override
    public ClientPlayNetworkHandler getNetworkHandler() {
        return networkHandler;
    }
}
