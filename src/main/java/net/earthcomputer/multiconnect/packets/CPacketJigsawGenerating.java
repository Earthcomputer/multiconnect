package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;

@MessageVariant(minVersion = Protocols.V1_16)
public class CPacketJigsawGenerating {
    public CommonTypes.BlockPos pos;
    public int levels;
    public boolean keepJigsaws;

    @Handler(protocol = Protocols.V1_15_2)
    public static void drop() {
    }
}
