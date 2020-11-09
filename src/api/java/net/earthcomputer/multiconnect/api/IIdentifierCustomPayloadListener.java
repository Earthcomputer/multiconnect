package net.earthcomputer.multiconnect.api;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * @deprecated Use {@link ICustomPayloadListener} instead.
 */
@Deprecated
@FunctionalInterface
public interface IIdentifierCustomPayloadListener {
    void onCustomPayload(int protocol, Identifier channel, PacketByteBuf data);
}
