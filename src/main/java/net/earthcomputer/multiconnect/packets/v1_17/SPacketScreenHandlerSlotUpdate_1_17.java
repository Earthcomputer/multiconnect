package net.earthcomputer.multiconnect.packets.v1_17;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketScreenHandlerSlotUpdate;

@Message(variantOf = SPacketScreenHandlerSlotUpdate.class, maxVersion = Protocols.V1_17)
public class SPacketScreenHandlerSlotUpdate_1_17 {
    public byte syncId;
    public short slot;
    public CommonTypes.ItemStack stack;
}
