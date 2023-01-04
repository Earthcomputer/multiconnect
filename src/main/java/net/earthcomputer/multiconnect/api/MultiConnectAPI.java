package net.earthcomputer.multiconnect.api;

import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The MultiConnect API
 */
public class MultiConnectAPI {

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
    public int getProtocolVersion() {
        return SharedConstants.getCurrentVersion().getProtocolVersion();
    }

    /**
     * Gets a supported {@link IProtocol} object by its protocol version, or {@code null} if the protocol is not supported
     */
    @ThreadSafe
    public IProtocol byProtocolVersion(int version) {
        return version == SharedConstants.getCurrentVersion().getProtocolVersion() ? CurrentVersionProtocol.INSTANCE : null;
    }

    /**
     * Returns a list of supported protocols, from newest to oldest
     */
    @ThreadSafe
    public List<IProtocol> getSupportedProtocols() {
        return Collections.singletonList(CurrentVersionProtocol.INSTANCE);
    }

    /**
     * Returns whether the given registry contains the given value on the server.
     */
    @ThreadSafe
    public <T> boolean doesServerKnow(Registry<T> registry, T value) {
        return registry.getResourceKey(value).isPresent();
    }

    /**
     * Returns whether the given registry contains the given value on the server.
     */
    @ThreadSafe
    public <T> boolean doesServerKnow(Registry<T> registry, ResourceKey<T> key) {
        return key.isFor(registry.key()) && registry.getOptional(key.location()).isPresent();
    }

    //region deprecated methods

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public void addClientboundIdentifierCustomPayloadListener(ICustomPayloadListener<ResourceLocation> listener) {
        // overridden by protocol impl
    }

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public void removeClientboundIdentifierCustomPayloadListener(ICustomPayloadListener<ResourceLocation> listener) {
        // overridden by protocol impl
    }

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public void addClientboundStringCustomPayloadListener(ICustomPayloadListener<String> listener) {
        // overridden by protocol impl
    }

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public void removeClientboundStringCustomPayloadListener(ICustomPayloadListener<String> listener) {
        // overridden by protocol impl
    }

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    @Contract("null, _, _ -> fail")
    @ThreadSafe
    public void forceSendCustomPayload(ClientPacketListener listener, ResourceLocation channel, FriendlyByteBuf data) {
        if (listener == null) {
            throw new IllegalArgumentException("Trying to send custom payload when not in-game");
        }
        listener.send(new ServerboundCustomPayloadPacket(channel, data));
    }

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    @Contract("null, _, _ -> fail")
    @ThreadSafe
    public void forceSendStringCustomPayload(ClientPacketListener listener, String channel, FriendlyByteBuf data) {
        throw new IllegalStateException("Trying to send custom payload to " + SharedConstants.getCurrentVersion().getName() + " server");
    }

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public void addServerboundIdentifierCustomPayloadListener(ICustomPayloadListener<ResourceLocation> listener) {
        // overridden by protocol impl
    }

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public void removeServerboundIdentifierCustomPayloadListener(ICustomPayloadListener<ResourceLocation> listener) {
        // overridden by protocol impl
    }

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public void addServerboundStringCustomPayloadListener(ICustomPayloadListener<String> listener) {
        // overridden by protocol impl
    }

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public void removeServerboundStringCustomPayloadListener(ICustomPayloadListener<String> listener) {
        // overridden by protocol impl
    }

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public void addIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener) {
        // overridden by protocol impl
    }

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public void removeIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener) {
        // overridden by protocol impl
    }

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public void addStringCustomPayloadListener(IStringCustomPayloadListener listener) {
        // overridden by protocol impl
    }

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public void removeStringCustomPayloadListener(IStringCustomPayloadListener listener) {
        // overridden by protocol impl
    }

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public void forceSendCustomPayload(ResourceLocation channel, FriendlyByteBuf data) {
        forceSendCustomPayload(Minecraft.getInstance().getConnection(), channel, data);
    }

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public void forceSendStringCustomPayload(String channel, FriendlyByteBuf data) {
        forceSendStringCustomPayload(Minecraft.getInstance().getConnection(), channel, data);
    }

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public void addServerboundIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener) {
        // overridden by protocol impl
    }

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public void removeServerboundIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener) {
        // overridden by protocol impl
    }

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public void addServerboundStringCustomPayloadListener(IStringCustomPayloadListener listener) {
        // overridden by protocol impl
    }

    /**
     * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
     */
    @Deprecated
    public void removeServerboundStringCustomPayloadListener(IStringCustomPayloadListener listener) {
        // overridden by protocol impl
    }

    //endregion

    //region internal

    private static final MultiConnectAPI INSTANCE;
    static {
        MultiConnectAPI api;
        try {
            api = (MultiConnectAPI) Class.forName("net.earthcomputer.multiconnect.impl.APIImpl").getConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            api = new MultiConnectAPI();
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
        INSTANCE = api;
    }

    private static class CurrentVersionProtocol implements IProtocol {
        public static CurrentVersionProtocol INSTANCE = new CurrentVersionProtocol();

        private String majorReleaseName;

        @Override
        public int getValue() {
            return SharedConstants.getCurrentVersion().getProtocolVersion();
        }

        @Override
        public String getName() {
            return SharedConstants.getCurrentVersion().getName();
        }

        @Override
        public int getDataVersion() {
            return SharedConstants.getCurrentVersion().getWorldVersion();
        }

        @Override
        public boolean isMajorRelease() {
            return true;
        }

        @Override
        public IProtocol getMajorRelease() {
            return this;
        }

        @Override
        public String getMajorReleaseName() {
            if (majorReleaseName == null) {
                // take everything before the second dot, if there is one
                Matcher matcher = Pattern.compile("([^.]*(\\.[^.]*)?).*").matcher(getName());
                boolean matches = matcher.matches();
                assert matches;
                majorReleaseName = matcher.group(1);
            }
            return majorReleaseName;
        }

        @Override
        public List<IProtocol> getMinorReleases() {
            return Collections.singletonList(this);
        }

        @Override
        public boolean isMulticonnectBeta() {
            return false;
        }

        @Override
        public boolean isMulticonnectExtension() {
            return false;
        }
    }

    //endregion

}
