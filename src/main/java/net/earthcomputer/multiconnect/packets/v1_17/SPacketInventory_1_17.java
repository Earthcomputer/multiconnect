package net.earthcomputer.multiconnect.packets.v1_17;

import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.packets.CommonTypes;

import java.util.List;

@Message
public class SPacketInventory_1_17 {
    @Type(Types.UNSIGNED_BYTE)
    public int syncId;
    @Length(type = Types.SHORT)
    public List<CommonTypes.ItemStack> slots;
}
