package net.earthcomputer.multiconnect.api;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
 */
@Deprecated
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
