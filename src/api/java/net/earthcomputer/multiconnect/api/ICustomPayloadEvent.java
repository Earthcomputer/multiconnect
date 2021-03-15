package net.earthcomputer.multiconnect.api;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

/**
 * An event fired when custom payloads to or from older servers are blocked by multiconnect.
 *
 * @param <T> The type of the channel, either {@linkplain net.minecraft.util.Identifier Identifier} or
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
    PacketByteBuf getData();

    /**
     * The {@linkplain ClientPlayNetworkHandler} that is being sent to or from.
     */
    ClientPlayNetworkHandler getNetworkHandler();
}
