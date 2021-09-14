package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class SPacketScreenHandlerSlotUpdate {
    public byte syncId;
    public int revision;
    public short slot;
    public CommonTypes.ItemStack stack;
}
