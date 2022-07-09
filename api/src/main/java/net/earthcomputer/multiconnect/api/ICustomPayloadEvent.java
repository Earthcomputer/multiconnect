package net.earthcomputer.multiconnect.api;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * An event fired when custom payloads to or from older servers are blocked by multiconnect.
 *
 * @param <T> The type of the channel, either {@linkplain ResourceLocation} or
 *              {@linkplain String}.
 *
 * @see ICustomPayloadListener
 */
public interface ICustomPayloadEvent<T> {
    /**
     * The protocol version that the server is on, to be compared with values in {@link Protocols}.
     */
    int getProtocol();

    /**
     * The custom payload channel.
     */
    T getChannel();

    /**
     * The payload itself.
     */
    FriendlyByteBuf getData();

    /**
     * The {@linkplain ClientPacketListener} that is being sent to or from.
     */
    ClientPacketListener getConnection();

    /**
     * @deprecated Use {@link #getConnection()} instead.
     */
    @Deprecated
    default ClientPacketListener getNetworkHandler() {
        return getConnection();
    }
}
