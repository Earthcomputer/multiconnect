package net.earthcomputer.multiconnect.provider;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.VersionProvider;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;

public class MulticonnectVersionProvider implements VersionProvider {
    @Override
    public int getClosestServerProtocol(UserConnection connection) throws Exception {
        return ConnectionInfo.protocolVersion;
    }
}
