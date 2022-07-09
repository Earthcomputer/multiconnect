package net.earthcomputer.multiconnect.packets.v1_17;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketContainerSetSlot;

@MessageVariant(maxVersion = Protocols.V1_17)
public class SPacketContainerSetSlot_1_17 implements SPacketContainerSetSlot {
    public byte syncId;
    public short slot;
    public CommonTypes.ItemStack stack;
}
