package net.earthcomputer.multiconnect.api;

import net.minecraft.util.SharedConstants;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.List;

/**
 * The MultiConnect API
 */
@Mod("multiconnect-api")
public class MultiConnectAPI {

    static MultiConnectAPI INSTANCE = new MultiConnectAPI();

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
        return SharedConstants.getVersion().getProtocolVersion();
    }

    /**
     * Gets a supported {@link IProtocol} object by its protocol version, or <tt>null</tt> if the protocol is not supported
     */
    public IProtocol byProtocolVersion(int version) {
        return version == SharedConstants.getVersion().getProtocolVersion() ? CurrentVersionProtocol.INSTANCE : null;
    }

    /**
     * Returns a list of supported protocols, from newest to oldest
     */
    public List<IProtocol> getSupportedProtocols() {
        return Collections.singletonList(CurrentVersionProtocol.INSTANCE);
    }

    private static class CurrentVersionProtocol implements IProtocol {
        public static CurrentVersionProtocol INSTANCE = new CurrentVersionProtocol();

        @Override
        public int getValue() {
            return SharedConstants.getVersion().getProtocolVersion();
        }

        @Override
        public String getName() {
            return SharedConstants.getVersion().getName();
        }
    }

}
