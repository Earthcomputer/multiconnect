package net.earthcomputer.multiconnect.impl;

import net.earthcomputer.multiconnect.api.ICustomPayloadEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

public record CustomPayloadEvent<T>(
        int protocol,
        T channel,
        PacketByteBuf data,
        ClientPlayNetworkHandler networkHandler
) implements ICustomPayloadEvent<T> {
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
