package net.earthcomputer.multiconnect;

import net.earthcomputer.multiconnect.protocol.IProtocol;
import net.earthcomputer.multiconnect.protocol.Protocols;
import net.minecraft.SharedConstants;

public class ConnectionInfo {

    public static String ip;
    public static int port = -1;
    public static int protocolVersion = SharedConstants.getGameVersion().getProtocolVersion();
    public static IProtocol protocol = Protocols.latest();

}
