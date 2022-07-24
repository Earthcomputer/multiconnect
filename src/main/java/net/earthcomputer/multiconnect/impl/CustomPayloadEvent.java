package net.earthcomputer.multiconnect.impl;

import net.earthcomputer.multiconnect.api.ICustomPayloadEvent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

@Deprecated
public record CustomPayloadEvent<T>(
        int protocol,
        T channel,
        FriendlyByteBuf data,
        ClientPacketListener connection
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
    public FriendlyByteBuf getData() {
        return data;
    }

    @Override
    public ClientPacketListener getConnection() {
        return connection;
    }
}
