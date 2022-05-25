package net.earthcomputer.multiconnect.protocols.v1_14_3;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketSetTradeOffers;
public class Trade_1_14_3 implements SPacketSetTradeOffers.Trade {
    @Type(Types.INT)
    public int demand;
}
