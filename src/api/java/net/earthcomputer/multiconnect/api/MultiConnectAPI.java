package net.earthcomputer.multiconnect.api;

import net.minecraft.SharedConstants;

import java.util.Collections;
import java.util.List;

/**
 * The MultiConnect API
 */
public class MultiConnectAPI {

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
     * Adds an {@link IIdentifierCustomPayloadListener}. Adding one of these listeners allows for mods to listen to
     * non-vanilla {@link net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket CustomPayloadS2CPacket}s sent
     * by servers on older versions. Such packets are blocked by multiconnect from normal handling.
     *
     * <p>This listener is not called for custom payloads from servers on the current version, as multiconnect does not
     * block those. This listener is only called for servers on 1.13 and above; use
     * {@link #addStringCustomPayloadListener(IStringCustomPayloadListener)} if you want to listen to custom payloads
     * from older servers.</p>
     */
    public void addIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener) {
        // overridden by protocol impl
    }

    /**
     * Removes an {@link IIdentifierCustomPayloadListener}.
     * @see #addIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener)
     */
    public void removeIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener) {
        // overridden by protocol impl
    }

    /**
     * Adds an {@link IStringCustomPayloadListener}. Adding one of these listeners allows for mods to listen to
     * non-vanilla {@link net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket CustomPayloadS2CPacket}s sent
     * by servers on 1.12.2 or below. Such packets are blocked by multiconnect from normal handling.
     *
     * <p>This listener is not called for custom payloads from servers on 1.13 or above. To listen for these custom
     * payloads, use {@link #addIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener)}, or if the server is
     * on the current Minecraft version, simply do it the normal way.</p>
     */
    public void addStringCustomPayloadListener(IStringCustomPayloadListener listener) {
        // overridden by protocol impl
    }

    /**
     * Removes an {@link IStringCustomPayloadListener}.
     * @see #addStringCustomPayloadListener(IStringCustomPayloadListener)
     */
    public void removeStringCustomPayloadListener(IStringCustomPayloadListener listener) {
        // overridden by protocol impl
    }

    private static class CurrentVersionProtocol implements IProtocol {
        public static CurrentVersionProtocol INSTANCE = new CurrentVersionProtocol();

        @Override
        public int getValue() {
            return SharedConstants.getGameVersion().getProtocolVersion();
        }

        @Override
        public String getName() {
            return SharedConstants.getGameVersion().getName();
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
        public List<IProtocol> getMinorReleases() {
            return Collections.singletonList(this);
        }
    }

}
