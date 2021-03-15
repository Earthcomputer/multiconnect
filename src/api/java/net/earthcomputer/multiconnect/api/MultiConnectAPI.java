package net.earthcomputer.multiconnect.api;

import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

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
    public static MultiConnectAPI instance() {
        return INSTANCE;
    }

    /**
     * Gets the protocol version of the server currently connected to,
     * or the current game version if not connected to a server
     */
    public int getProtocolVersion() {
        return SharedConstants.getGameVersion().getProtocolVersion();
    }

    /**
     * Gets a supported {@link IProtocol} object by its protocol version, or <tt>null</tt> if the protocol is not supported
     */
    public IProtocol byProtocolVersion(int version) {
        return version == SharedConstants.getGameVersion().getProtocolVersion() ? CurrentVersionProtocol.INSTANCE : null;
    }

    /**
     * Returns a list of supported protocols, from newest to oldest
     */
    public List<IProtocol> getSupportedProtocols() {
        return Collections.singletonList(CurrentVersionProtocol.INSTANCE);
    }

    /**
     * Adds a clientbound {@link ICustomPayloadListener ICustomPayloadListener&lt;Identifier&gt;}. Adding one of these
     * listeners allows for mods to listen to non-vanilla
     * {@link net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket CustomPayloadS2CPacket}s sent by servers on
     * older versions. Such packets are blocked by multiconnect from normal handling.
     *
     * <p>This listener is not called for custom payloads from servers on the current version, as multiconnect does not
     * block those. This listener is only called for servers on 1.13 and above; use
     * {@link #addClientboundStringCustomPayloadListener(ICustomPayloadListener)} if you want to listen to custom
     * payloads from older servers.</p>
     */
    public void addClientboundIdentifierCustomPayloadListener(ICustomPayloadListener<Identifier> listener) {
        // overridden by protocol impl
    }

    /**
     * Removes a clientbound {@link ICustomPayloadListener ICustomPayloadListener&lt;Identifier&gt;}.
     * @see #addClientboundIdentifierCustomPayloadListener(ICustomPayloadListener)
     */
    public void removeClientboundIdentifierCustomPayloadListener(ICustomPayloadListener<Identifier> listener) {
        // overridden by protocol impl
    }

    /**
     * Adds a clientbound {@link ICustomPayloadListener ICustomPayloadListener&lt;String&gt;}. Adding one of these
     * listeners allows for mods to listen to non-vanilla
     * {@link net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket CustomPayloadS2CPacket}s sent by servers on
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
     * @param networkHandler The network handler
     * @param channel The channel to send data on
     * @param data The data to send
     */
    public void forceSendCustomPayload(ClientPlayNetworkHandler networkHandler, Identifier channel, PacketByteBuf data) {
        if (networkHandler == null) {
            throw new IllegalArgumentException("Trying to send custom payload when not in-game");
        }
        networkHandler.sendPacket(new CustomPayloadC2SPacket(channel, data));
    }

    /**
     * By default, multiconnect blocks non-vanilla client-to-server custom payload packets.
     * Use this method to send one anyway to a 1.12.2 or below server.
     *
     * @param networkHandler The network handler
     * @param channel The channel to send data on
     * @param data The data to send
     */
    public void forceSendStringCustomPayload(ClientPlayNetworkHandler networkHandler, String channel, PacketByteBuf data) {
        throw new IllegalStateException("Trying to send custom payload to " + SharedConstants.getGameVersion().getName() + " server");
    }

    /**
     * Adds a serverbound {@link ICustomPayloadListener ICustomPayloadListener&lt;Identifier&gt;}. Adding one of these
     * listeners allows for mods to listen to non-vanilla
     * {@link net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket CustomPayloadS2CPacket}s sent by clientside
     * mods to servers on older versions. Such packets are blocked by multiconnect from being sent.
     *
     * <p>This listener is not called for custom payloads to servers on the current version, as multiconnect does not
     * block those. This listener is only called for servers on 1.13 and above; use
     * {@link #addServerboundStringCustomPayloadListener(ICustomPayloadListener)} if you want to listen to custom
     * payloads to older servers. This listener is also not called for custom payloads on vanilla channels, as these are
     * also not blocked by multiconnect.</p>
     */
    public void addServerboundIdentifierCustomPayloadListener(ICustomPayloadListener<Identifier> listener) {
        // overridden by protocol impl
    }

    /**
     * Removes a serverbound {@link ICustomPayloadListener ICustomPayloadListener&lt;Identifier&gt;}.
     * @see #addServerboundIdentifierCustomPayloadListener(ICustomPayloadListener)
     */
    public void removeServerboundIdentifierCustomPayloadListener(ICustomPayloadListener<Identifier> listener) {
        // overridden by protocol impl
    }

    /**
     * Adds a serverbound {@link ICustomPayloadListener ICustomPayloadListener&lt;String&gt;}. Adding one of these
     * listeners allows for mods to listen to non-vanilla
     * {@link net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket CustomPayloadS2CPacket}s sent by clientside
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
    public <T> boolean doesServerKnow(Registry<T> registry, T value) {
        return registry.getKey(value).isPresent();
    }

    /**
     * Returns whether the given registry contains the given value on the server.
     */
    public <T> boolean doesServerKnow(Registry<T> registry, RegistryKey<T> key) {
        return key.isOf(registry.getKey()) && registry.getOrEmpty(key.getValue()).isPresent();
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
     * @deprecated Use {@link #forceSendCustomPayload(ClientPlayNetworkHandler, Identifier, PacketByteBuf)} instead.
     *              {@code MinecraftClient.getInstance().getNetworkHandler()} can return null before the game join
     *              packet has been received.
     */
    @Deprecated
    public void forceSendCustomPayload(Identifier channel, PacketByteBuf data) {
        forceSendCustomPayload(MinecraftClient.getInstance().getNetworkHandler(), channel, data);
    }

    /**
     * @deprecated Use {@link #forceSendStringCustomPayload(ClientPlayNetworkHandler, String, PacketByteBuf)} instead.
     *              {@code MinecraftClient.getInstance().getNetworkHandler()} can return null before the game join
     *              packet has been received.
     */
    @Deprecated
    public void forceSendStringCustomPayload(String channel, PacketByteBuf data) {
        forceSendStringCustomPayload(MinecraftClient.getInstance().getNetworkHandler(), channel, data);
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
            return SharedConstants.getGameVersion().getProtocolVersion();
        }

        @Override
        public String getName() {
            return SharedConstants.getGameVersion().getName();
        }

        @Override
        public int getDataVersion() {
            return SharedConstants.getGameVersion().getWorldVersion();
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
