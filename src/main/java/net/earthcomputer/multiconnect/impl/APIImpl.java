package net.earthcomputer.multiconnect.impl;

import net.earthcomputer.multiconnect.api.IIdentifierCustomPayloadListener;
import net.earthcomputer.multiconnect.api.IProtocol;
import net.earthcomputer.multiconnect.api.IStringCustomPayloadListener;
import net.earthcomputer.multiconnect.api.MultiConnectAPI;
import net.earthcomputer.multiconnect.connect.ConnectionMode;
import net.earthcomputer.multiconnect.protocols.generic.CustomPayloadHandler;

import java.util.Arrays;
import java.util.List;

public class APIImpl extends MultiConnectAPI {
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

    @Override
    public void addIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener) {
        CustomPayloadHandler.addIdentifierCustomPayloadListener(listener);
    }

    @Override
    public void removeIdentifierCustomPayloadListener(IIdentifierCustomPayloadListener listener) {
        CustomPayloadHandler.removeIdentifierCustomPayloadListener(listener);
    }

    @Override
    public void addStringCustomPayloadListener(IStringCustomPayloadListener listener) {
        CustomPayloadHandler.addStringCustomPayloadListener(listener);
    }

    @Override
    public void removeStringCustomPayloadListener(IStringCustomPayloadListener listener) {
        CustomPayloadHandler.removeStringCustomPayloadListener(listener);
    }
}
