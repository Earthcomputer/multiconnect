package net.earthcomputer.multiconnect.api;

import net.minecraft.network.PacketByteBuf;

/**
 * A listener for custom payloads using {@linkplain String} channels.
 * @see MultiConnectAPI#addStringCustomPayloadListener(IStringCustomPayloadListener) 
 */
@FunctionalInterface
public interface IStringCustomPayloadListener {
    /**
     * Called on a custom payload on a server version older than 1.13 (exclusive).
     */
    void onCustomPayload(int protocol, String channel, PacketByteBuf data);
}
