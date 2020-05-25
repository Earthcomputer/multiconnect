package net.earthcomputer.multiconnect.api;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * A listener for custom payloads using {@link Identifier} channels.
 * @see MultiConnectAPI#addIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener)
 */
@FunctionalInterface
public interface IIdentifierCustomPayloadListener {
    /**
     * Called on a custom payload on a server version between 1.13 (inclusive) and the current version (exclusive)
     */
    void onCustomPayload(int protocol, Identifier channel, PacketByteBuf data);
}
