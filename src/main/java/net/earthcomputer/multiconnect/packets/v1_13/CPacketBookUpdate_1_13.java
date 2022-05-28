package net.earthcomputer.multiconnect.packets.v1_13;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketBookUpdate;
import net.earthcomputer.multiconnect.packets.CommonTypes;

@MessageVariant(maxVersion = Protocols.V1_13)
public class CPacketBookUpdate_1_13 implements CPacketBookUpdate {
    public CommonTypes.ItemStack stack;
    public boolean sign;
}
