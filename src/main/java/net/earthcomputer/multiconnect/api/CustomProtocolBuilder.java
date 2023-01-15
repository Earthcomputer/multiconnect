package net.earthcomputer.multiconnect.api;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface CustomProtocolBuilder {
    CustomProtocolBuilder majorVersion();
    CustomProtocolBuilder markBeta();
    CustomProtocolBuilder behavior(ProtocolBehavior behavior);
    CustomProtocolBuilder majorReleaseName(String name);
    IProtocol register();
}
