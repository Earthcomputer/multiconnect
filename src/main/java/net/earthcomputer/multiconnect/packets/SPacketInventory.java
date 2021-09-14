package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Protocol;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.v1_17.SPacketInventory_1_17;

import java.util.List;

@Message(translateFromOlder = @Protocol(value = Protocols.V1_17, type = SPacketInventory_1_17.class))
public class SPacketInventory {
    @Type(Types.UNSIGNED_BYTE)
    public int syncId;
    @Introduce(intValue = 0)
    public int revision;
    public List<CommonTypes.ItemStack> slots;
    @Introduce(defaultConstruct = true)
    public CommonTypes.ItemStack cursorStack;
}
