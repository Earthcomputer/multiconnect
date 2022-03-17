package net.earthcomputer.multiconnect.packets.v1_17;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketScreenHandlerSlotUpdate;

@MessageVariant(maxVersion = Protocols.V1_17)
public class SPacketScreenHandlerSlotUpdate_1_17 implements SPacketScreenHandlerSlotUpdate {
    public byte syncId;
    public short slot;
    public CommonTypes.ItemStack stack;
}
