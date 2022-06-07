package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class CPacketCreativeInventoryAction {
    public short slot;
    public CommonTypes.ItemStack stack;
}
