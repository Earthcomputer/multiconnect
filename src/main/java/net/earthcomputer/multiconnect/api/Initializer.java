package net.earthcomputer.multiconnect.api;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.ConnectionMode;
import net.fabricmc.api.ModInitializer;

import java.util.Arrays;
import java.util.List;

// In the api package so we can access MultiConnectAPI.INSTANCE
public class Initializer implements ModInitializer {
    @Override
    public void onInitialize() {
        MultiConnectAPI.INSTANCE = new MultiConnectAPI() {
            @Override
            public int getProtocolVersion() {
                return ConnectionInfo.protocolVersion;
            }

            @Override
            public IProtocol byProtocolVersion(int version) {
                ConnectionMode protocol = ConnectionMode.byValue(version);
                return protocol == ConnectionMode.AUTO ? null : protocol;
            }

            @Override
            public List<IProtocol> getSupportedProtocols() {
                return Arrays.asList(ConnectionMode.protocolValues());
            }
        };
    }
}
