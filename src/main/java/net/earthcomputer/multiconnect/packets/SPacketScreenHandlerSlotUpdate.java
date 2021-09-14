package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Protocol;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.v1_17.SPacketScreenHandlerSlotUpdate_1_17;

@Message(translateFromOlder = @Protocol(value = Protocols.V1_17, type = SPacketScreenHandlerSlotUpdate_1_17.class))
public class SPacketScreenHandlerSlotUpdate {
    public byte syncId;
    @Introduce(intValue = 0)
    public int revision;
    public short slot;
    public CommonTypes.ItemStack stack;
}
