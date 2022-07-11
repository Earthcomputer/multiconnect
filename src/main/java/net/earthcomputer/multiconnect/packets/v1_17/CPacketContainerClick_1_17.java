package net.earthcomputer.multiconnect.packets.v1_17;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketContainerClick;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.latest.CPacketContainerClick_Latest;

import java.util.List;

@MessageVariant(minVersion = Protocols.V1_17, maxVersion = Protocols.V1_17)
public class CPacketContainerClick_1_17 implements CPacketContainerClick {
    @Type(Types.UNSIGNED_BYTE)
    public int syncId;
    public short slot;
    public byte button;
    public CPacketContainerClick_Latest.Mode mode;
    public List<CPacketContainerClick_Latest.Slot> slots;
    public CommonTypes.ItemStack clickedItem;
}
