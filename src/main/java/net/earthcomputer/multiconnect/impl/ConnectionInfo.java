package net.earthcomputer.multiconnect.impl;

import net.earthcomputer.multiconnect.protocols.generic.AbstractProtocol;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.minecraft.SharedConstants;

public class ConnectionInfo {

    public static int protocolVersion = SharedConstants.getGameVersion().getProtocolVersion();
    public static AbstractProtocol protocol = ProtocolRegistry.latest();

}
