package net.earthcomputer.multiconnect.packets.v1_17;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketClickSlot;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.latest.CPacketClickSlot_Latest;

import java.util.List;

@MessageVariant(maxVersion = Protocols.V1_17)
public class CPacketClickSlot_1_17 implements CPacketClickSlot {
    @Type(Types.UNSIGNED_BYTE)
    public int syncId;
    public short slot;
    public byte button;
    public CPacketClickSlot_Latest.Mode mode;
    public List<CPacketClickSlot_Latest.Slot> slots;
    public CommonTypes.ItemStack clickedItem;
}
