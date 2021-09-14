package net.earthcomputer.multiconnect.packets.v1_17;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.packets.CommonTypes;

@Message
public class SPacketScreenHandlerSlotUpdate_1_17 {
    public byte syncId;
    public short slot;
    public CommonTypes.ItemStack stack;
}
