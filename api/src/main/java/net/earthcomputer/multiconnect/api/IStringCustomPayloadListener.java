package net.earthcomputer.multiconnect.api;

import net.minecraft.network.FriendlyByteBuf;

/**
 * @deprecated Use {@link ICustomPayloadListener} instead.
 */
@Deprecated
@FunctionalInterface
public interface IStringCustomPayloadListener {
    void onCustomPayload(int protocol, String channel, FriendlyByteBuf data);
}
