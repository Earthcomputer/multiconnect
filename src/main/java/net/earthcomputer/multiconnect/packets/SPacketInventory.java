package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;

import java.util.List;

@Message(minVersion = Protocols.V1_17_1)
public class SPacketInventory {
    @Type(Types.UNSIGNED_BYTE)
    public int syncId;
    @Introduce(intValue = 0)
    public int revision;
    public List<CommonTypes.ItemStack> slots;
    @Introduce(defaultConstruct = true)
    public CommonTypes.ItemStack cursorStack;
}
