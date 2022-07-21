package net.earthcomputer.multiconnect.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * @deprecated Use {@link ICustomPayloadListener} instead.
 */
@Deprecated
@FunctionalInterface
public interface IIdentifierCustomPayloadListener {
    void onCustomPayload(int protocol, ResourceLocation channel, FriendlyByteBuf data);
}
