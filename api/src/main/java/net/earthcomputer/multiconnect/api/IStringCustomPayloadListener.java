package net.earthcomputer.multiconnect.api;

import net.minecraft.network.PacketByteBuf;

/**
 * @deprecated Use {@link ICustomPayloadListener} instead.
 */
@Deprecated
@FunctionalInterface
public interface IStringCustomPayloadListener {
    void onCustomPayload(int protocol, String channel, PacketByteBuf data);
}
