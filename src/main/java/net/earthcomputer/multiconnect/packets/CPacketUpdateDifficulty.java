package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;

@MessageVariant(minVersion = Protocols.V1_14)
public class CPacketUpdateDifficulty {
    public byte difficulty;

    @Handler(protocol = Protocols.V1_13_2)
    public static void drop() {
    }
}
