package net.earthcomputer.multiconnect.api;

import net.minecraft.SharedConstants;

public class MultiConnectAPI {

    static MultiConnectAPI INSTANCE = new MultiConnectAPI();

    public static MultiConnectAPI instance() {
        return INSTANCE;
    }

    public int getProtocolVersion() {
        return SharedConstants.getGameVersion().getProtocolVersion();
    }

}
