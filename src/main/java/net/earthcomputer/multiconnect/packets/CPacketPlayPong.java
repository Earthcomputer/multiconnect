package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;

@MessageVariant
public class CPacketPlayPong {
    @Type(Types.INT)
    public int parameter;

    @Handler(protocol = Protocols.V1_16_5)
    public static void drop() {
    }
}
