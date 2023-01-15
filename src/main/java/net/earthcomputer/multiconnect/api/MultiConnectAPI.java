package net.earthcomputer.multiconnect.api;

import net.earthcomputer.multiconnect.impl.APIImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.util.List;

/**
 * The MultiConnect API
 */
@ApiStatus.NonExtendable
public abstract class MultiConnectAPI {
    // can't be changed to an interface for backwards compatibility reasons

    /**
     * Returns the singleton instance of this API
     */
    @ThreadSafe
    public static MultiConnectAPI instance() {
        return INSTANCE;
    }

    /**
     * Gets the protocol version of the server currently connected to,
     * or the current game version if not connected to a server
     */
    @ThreadSafe
    public abstract int getProtocolVersion();

    /**
     * Gets a supported {@link IProtocol} object by its protocol version, or {@code null} if the protocol is not supported
     */
    @ThreadSafe
    public abstract IProtocol byProtocolVersion(int version);

    /**
     * Returns a list of supported protocols, from newest to oldest
     */
    @ThreadSafe
    public abstract List<IProtocol> getSupportedProtocols();

    public abstract CustomProtocolBuilder createCustomProtocol(int version, String name, int dataVersion);

    /**
     * Returns whether the given registry contains the given value on the server.
     */
    @ThreadSafe
    public abstract <T> boolean doesServerKnow(Registry<T> registry, T value);

    /**
     * Returns whether the given registry contains the given value on the server.
     */
    @ThreadSafe
    public abstract <T> boolean doesServerKnow(Registry<T> registry, ResourceKey<T> key);

    //region deprecated methods

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public abstract void addClientboundIdentifierCustomPayloadListener(ICustomPayloadListener<ResourceLocation> listener);

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public abstract void removeClientboundIdentifierCustomPayloadListener(ICustomPayloadListener<ResourceLocation> listener);

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public abstract void addClientboundStringCustomPayloadListener(ICustomPayloadListener<String> listener);

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public abstract void removeClientboundStringCustomPayloadListener(ICustomPayloadListener<String> listener);

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    @Contract("null, _, _ -> fail")
    @ThreadSafe
    public abstract void forceSendCustomPayload(ClientPacketListener listener, ResourceLocation channel, FriendlyByteBuf data);

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    @Contract("null, _, _ -> fail")
    @ThreadSafe
    public abstract void forceSendStringCustomPayload(ClientPacketListener listener, String channel, FriendlyByteBuf data);

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public abstract void addServerboundIdentifierCustomPayloadListener(ICustomPayloadListener<ResourceLocation> listener);

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public abstract void removeServerboundIdentifierCustomPayloadListener(ICustomPayloadListener<ResourceLocation> listener);

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public abstract void addServerboundStringCustomPayloadListener(ICustomPayloadListener<String> listener);

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public abstract void removeServerboundStringCustomPayloadListener(ICustomPayloadListener<String> listener);

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public abstract void addIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener);

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public abstract void removeIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener);

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public abstract void addStringCustomPayloadListener(IStringCustomPayloadListener listener);

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public abstract void removeStringCustomPayloadListener(IStringCustomPayloadListener listener);

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public abstract void forceSendCustomPayload(ResourceLocation channel, FriendlyByteBuf data);

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public abstract void forceSendStringCustomPayload(String channel, FriendlyByteBuf data);

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public abstract void addServerboundIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener);

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public abstract void removeServerboundIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener);

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public abstract void addServerboundStringCustomPayloadListener(IStringCustomPayloadListener listener);

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public abstract void removeServerboundStringCustomPayloadListener(IStringCustomPayloadListener listener);

    //endregion

    private static final MultiConnectAPI INSTANCE = new APIImpl();

}
