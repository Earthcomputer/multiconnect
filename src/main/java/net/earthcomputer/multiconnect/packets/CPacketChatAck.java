package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.latest.SPacketPlayerChat_Latest;

@MessageVariant(minVersion = Protocols.V1_19_2)
public class CPacketChatAck {
    public SPacketPlayerChat_Latest.LastSeenUpdate lastSeen;

    @Handler(protocol = Protocols.V1_19)
    public static void drop() {
    }
}
