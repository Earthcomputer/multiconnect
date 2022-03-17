package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class SPacketEntity_1_16_5 {
    public int entityId;

    @Handler
    public static void drop() {
    }
}
