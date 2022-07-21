package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;

@MessageVariant(minVersion = Protocols.V1_19)
public class CPacketChatPreview {
    @Type(Types.INT)
    public int queryId;
    public String message;

    @Handler(protocol = Protocols.V1_18_2)
    public static void drop() {
    }
}
