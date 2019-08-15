package net.earthcomputer.multiconnect;

import net.earthcomputer.multiconnect.protocols.AbstractProtocol;
import net.earthcomputer.multiconnect.protocols.Protocols;
import net.minecraft.SharedConstants;

public class ConnectionInfo {

    public static String ip;
    public static int port = -1;
    public static int protocolVersion = SharedConstants.getGameVersion().getProtocolVersion();
    public static AbstractProtocol protocol = Protocols.latest();

}
