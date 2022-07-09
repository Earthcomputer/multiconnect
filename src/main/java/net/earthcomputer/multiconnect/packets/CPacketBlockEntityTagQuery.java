package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;

@MessageVariant(minVersion = Protocols.V1_13)
public class CPacketBlockEntityTagQuery {
    public int transactionId;
    public CommonTypes.BlockPos pos;

    @Handler(protocol = Protocols.V1_12_2)
    public static void drop() {
    }
}
