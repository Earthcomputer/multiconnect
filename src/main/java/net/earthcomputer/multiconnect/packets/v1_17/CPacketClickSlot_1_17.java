package net.earthcomputer.multiconnect.packets.v1_17;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Protocol;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketClickSlot;
import net.earthcomputer.multiconnect.packets.CommonTypes;

import java.util.List;

@Message(translateFromNewer = @Protocol(value = Protocols.V1_17_1, type = CPacketClickSlot.class))
public class CPacketClickSlot_1_17 {
    @Type(Types.UNSIGNED_BYTE)
    public int syncId;
    public short slot;
    public byte button;
    public CPacketClickSlot.Mode mode;
    public List<CPacketClickSlot.Slot> slots;
    public CommonTypes.ItemStack clickedItem;
}
