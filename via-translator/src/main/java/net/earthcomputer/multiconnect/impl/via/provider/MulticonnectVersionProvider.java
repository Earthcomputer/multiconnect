package net.earthcomputer.multiconnect.impl.via.provider;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.VersionProvider;
import net.earthcomputer.multiconnect.api.IMulticonnectTranslatorApi;

public class MulticonnectVersionProvider implements VersionProvider {
    private final IMulticonnectTranslatorApi api;

    public MulticonnectVersionProvider(IMulticonnectTranslatorApi api) {
        this.api = api;
    }

    @Override
    public int getClosestServerProtocol(UserConnection connection) {
        return api.getProtocolVersion();
    }
}
