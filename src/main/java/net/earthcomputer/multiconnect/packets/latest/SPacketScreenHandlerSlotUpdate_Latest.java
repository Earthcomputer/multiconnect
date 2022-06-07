package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketScreenHandlerSlotUpdate;

@MessageVariant(minVersion = Protocols.V1_17_1)
public class SPacketScreenHandlerSlotUpdate_Latest implements SPacketScreenHandlerSlotUpdate {
    public byte syncId;
    @Introduce(intValue = 0)
    public int revision;
    public short slot;
    public CommonTypes.ItemStack stack;
}
