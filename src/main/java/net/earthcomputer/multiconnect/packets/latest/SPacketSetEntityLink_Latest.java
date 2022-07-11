package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketSetEntityLink;

@MessageVariant(minVersion = Protocols.V1_14)
public class SPacketSetEntityLink_Latest implements SPacketSetEntityLink {
    @Type(Types.INT)
    public int attached;
    @Type(Types.INT)
    @Introduce(compute = "computeHolding")
    public int holding;

    public static int computeHolding(@Argument("holding") int holding) {
        return holding == -1 ? 0 : holding;
    }
}
