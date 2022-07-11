package net.earthcomputer.multiconnect.api;

import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
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
     * Adds a clientbound {@link ICustomPayloadListener ICustomPayloadListener&lt;ResourceLocation&gt;}. Adding one of
     * these listeners allows for mods to listen to non-vanilla
     * {@link ClientboundCustomPayloadPacket}s sent by servers on
     * older versions. Such packets are blocked by multiconnect from normal handling.
     *
     * <p>This listener is not called for custom payloads from servers on the current version, as multiconnect does not
     * block those. This listener is only called for servers on 1.13 and above; use
     * {@link #addClientboundStringCustomPayloadListener(ICustomPayloadListener)} if you want to listen to custom
     * payloads from older servers.</p>
     */
    public void addClientboundIdentifierCustomPayloadListener(ICustomPayloadListener<ResourceLocation> listener) {
        // overridden by protocol impl
    }

    /**
     * Removes a clientbound {@link ICustomPayloadListener ICustomPayloadListener&lt;ResourceLocation&gt;}.
     * @see #addClientboundIdentifierCustomPayloadListener(ICustomPayloadListener)
     */
    public void removeClientboundIdentifierCustomPayloadListener(ICustomPayloadListener<ResourceLocation> listener) {
        // overridden by protocol impl
    }

    /**
     * Adds a clientbound {@link ICustomPayloadListener ICustomPayloadListener&lt;String&gt;}. Adding one of these
     * listeners allows for mods to listen to non-vanilla
     * {@link ClientboundCustomPayloadPacket}s sent by servers on
     * 1.12.2 or below. Such packets are blocked by multiconnect from normal handling.
     *
     * <p>This listener is not called for custom payloads from servers on 1.13 or above. To listen for these custom
     * payloads, use {@link #addClientboundIdentifierCustomPayloadListener(ICustomPayloadListener)}, or if the server is
     * on the current Minecraft version, simply do it the normal way.</p>
     */
    public void addClientboundStringCustomPayloadListener(ICustomPayloadListener<String> listener) {
        // overridden by protocol impl
    }

    /**
     * Removes a clientbound {@link ICustomPayloadListener ICustomPayloadListener&lt;String&gt;}.
     * @see #addClientboundStringCustomPayloadListener(ICustomPayloadListener)
     */
    public void removeClientboundStringCustomPayloadListener(ICustomPayloadListener<String> listener) {
        // overridden by protocol impl
    }

    /**
     * By default, multiconnect blocks non-vanilla client-to-server custom payload packets.
     * Use this method to send one anyway.
     *
     * @param listener The packet listener
     * @param channel The channel to send data on
     * @param data The data to send
     */
    @Contract("null, _, _ -> fail")
    @ThreadSafe
    public void forceSendCustomPayload(ClientPacketListener listener, ResourceLocation channel, FriendlyByteBuf data) {
        if (listener == null) {
            throw new IllegalArgumentException("Trying to send custom payload when not in-game");
        }
        listener.send(new ServerboundCustomPayloadPacket(channel, data));
    }

    /**
     * By default, multiconnect blocks non-vanilla client-to-server custom payload packets.
     * Use this method to send one anyway to a 1.12.2 or below server.
     *
     * @param listener The packet listener
     * @param channel The channel to send data on
     * @param data The data to send
     */
    @Contract("null, _, _ -> fail")
    @ThreadSafe
    public void forceSendStringCustomPayload(ClientPacketListener listener, String channel, FriendlyByteBuf data) {
        throw new IllegalStateException("Trying to send custom payload to " + SharedConstants.getCurrentVersion().getName() + " server");
    }

    /**
     * Adds a serverbound {@link ICustomPayloadListener ICustomPayloadListener&lt;ResourceLocation&gt;}. Adding one of
     * these listeners allows for mods to listen to non-vanilla
     * {@link ServerboundCustomPayloadPacket}s sent by clientside
     * mods to servers on older versions. Such packets are blocked by multiconnect from being sent.
     *
     * <p>This listener is not called for custom payloads to servers on the current version, as multiconnect does not
     * block those. This listener is only called for servers on 1.13 and above; use
     * {@link #addServerboundStringCustomPayloadListener(ICustomPayloadListener)} if you want to listen to custom
     * payloads to older servers. This listener is also not called for custom payloads on vanilla channels, as these are
     * also not blocked by multiconnect.</p>
     */
    public void addServerboundIdentifierCustomPayloadListener(ICustomPayloadListener<ResourceLocation> listener) {
        // overridden by protocol impl
    }

    /**
     * Removes a serverbound {@link ICustomPayloadListener ICustomPayloadListener&lt;ResourceLocation&gt;}.
     * @see #addServerboundIdentifierCustomPayloadListener(ICustomPayloadListener)
     */
    public void removeServerboundIdentifierCustomPayloadListener(ICustomPayloadListener<ResourceLocation> listener) {
        // overridden by protocol impl
    }

    /**
     * Adds a serverbound {@link ICustomPayloadListener ICustomPayloadListener&lt;String&gt;}. Adding one of these
     * listeners allows for mods to listen to non-vanilla
     * {@link ServerboundCustomPayloadPacket}s sent by clientside
     * mods to servers on 1.12.2 or below. Such packets are blocked by multiconnect from being sent.
     *
     * <p>This listener is not called for custom payloads to servers on 1.13 or above. To listen for these custom
     * payloads, use {@link #addServerboundIdentifierCustomPayloadListener(ICustomPayloadListener)}, or if the server is
     * on the current Minecraft version, simply let the packet be sent the normal way. This listener is also not called
     * for custom payloads on vanilla channels, as these are also not blocked by multiconnect.</p>
     */
    public void addServerboundStringCustomPayloadListener(ICustomPayloadListener<String> listener) {
        // overridden by protocol impl
    }

    /**
     * Removes a serverbound {@link ICustomPayloadListener ICustomPayloadListener&lt;String&gt;}.
     * @see #addServerboundStringCustomPayloadListener(ICustomPayloadListener)
     */
    public void removeServerboundStringCustomPayloadListener(ICustomPayloadListener<String> listener) {
        // overridden by protocol impl
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
     * @deprecated Use {@link #addClientboundIdentifierCustomPayloadListener(ICustomPayloadListener)} instead.
     */
    @Deprecated
    public void addIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener) {
        // overridden by protocol impl
    }

    /**
     * @deprecated Use {@link #removeClientboundIdentifierCustomPayloadListener(ICustomPayloadListener)} instead.
     */
    @Deprecated
    public void removeIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener) {
        // overridden by protocol impl
    }

    /**
     * @deprecated Use {@link #addClientboundStringCustomPayloadListener(ICustomPayloadListener)} instead.
     */
    @Deprecated
    public void addStringCustomPayloadListener(IStringCustomPayloadListener listener) {
        // overridden by protocol impl
    }

    /**
     * @deprecated Use {@link #removeClientboundStringCustomPayloadListener(ICustomPayloadListener)} instead.
     */
    @Deprecated
    public void removeStringCustomPayloadListener(IStringCustomPayloadListener listener) {
        // overridden by protocol impl
    }

    /**
     * @deprecated Use {@link #forceSendCustomPayload(ClientPacketListener, ResourceLocation, FriendlyByteBuf)} instead.
     *              {@code Minecraft.getInstance().getConnection()} can return null before the game join
     *              packet has been received.
     */
    @Deprecated
    public void forceSendCustomPayload(ResourceLocation channel, FriendlyByteBuf data) {
        forceSendCustomPayload(Minecraft.getInstance().getConnection(), channel, data);
    }

    /**
     * @deprecated Use {@link #forceSendStringCustomPayload(ClientPacketListener, String, FriendlyByteBuf)} instead.
     *              {@code Minecraft.getInstance().getConnection()} can return null before the game join
     *              packet has been received.
     */
    @Deprecated
    public void forceSendStringCustomPayload(String channel, FriendlyByteBuf data) {
        forceSendStringCustomPayload(Minecraft.getInstance().getConnection(), channel, data);
    }

    /**
     * @deprecated Use {@link #addServerboundIdentifierCustomPayloadListener(ICustomPayloadListener)} instead.
     */
    @Deprecated
    public void addServerboundIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener) {
        // overridden by protocol impl
    }

    /**
     * @deprecated Use {@link #removeServerboundIdentifierCustomPayloadListener(ICustomPayloadListener)} instead.
     */
    @Deprecated
    public void removeServerboundIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener) {
        // overridden by protocol impl
    }

    /**
     * @deprecated Use {@link #addServerboundStringCustomPayloadListener(ICustomPayloadListener)} instead.
     */
    @Deprecated
    public void addServerboundStringCustomPayloadListener(IStringCustomPayloadListener listener) {
        // overridden by protocol impl
    }

    /**
     * @deprecated Use {@link #removeServerboundStringCustomPayloadListener(ICustomPayloadListener)} instead.
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
    }

    //endregion

}
