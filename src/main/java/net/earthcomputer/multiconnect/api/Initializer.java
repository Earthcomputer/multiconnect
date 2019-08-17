package net.earthcomputer.multiconnect.api;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.fabricmc.api.ModInitializer;

// In the api package so we can access MultiConnectAPI.INSTANCE
public class Initializer implements ModInitializer {
    @Override
    public void onInitialize() {
        MultiConnectAPI.INSTANCE = new MultiConnectAPI() {
            @Override
            public int getProtocolVersion() {
                return ConnectionInfo.protocolVersion;
            }
        };
    }
}
