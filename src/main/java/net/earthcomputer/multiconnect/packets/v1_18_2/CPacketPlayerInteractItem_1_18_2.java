package net.earthcomputer.multiconnect.packets.v1_18_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketPlayerInteractItem;
import net.earthcomputer.multiconnect.packets.CommonTypes;

@MessageVariant(maxVersion = Protocols.V1_18_2)
public class CPacketPlayerInteractItem_1_18_2 implements CPacketPlayerInteractItem {
    public CommonTypes.Hand hand;
}
