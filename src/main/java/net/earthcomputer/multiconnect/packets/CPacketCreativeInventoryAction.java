package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class CPacketCreativeInventoryAction {
    public short slot;
    public CommonTypes.ItemStack stack;
}
