package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.api.Protocols;

@Message(minVersion = Protocols.V1_17_1)
public class SPacketScreenHandlerSlotUpdate {
    public byte syncId;
    @Introduce(intValue = 0)
    public int revision;
    public short slot;
    public CommonTypes.ItemStack stack;
}
