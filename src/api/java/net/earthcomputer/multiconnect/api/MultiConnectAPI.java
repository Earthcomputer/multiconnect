package net.earthcomputer.multiconnect.api;

import net.minecraft.SharedConstants;

/**
 * The MultiConnect API
 */
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
        return SharedConstants.getGameVersion().getProtocolVersion();
    }

}
