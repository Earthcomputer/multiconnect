package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;

import java.util.List;
import java.util.Optional;

@Message
public class SPacketInventory {
    @Type(Types.UNSIGNED_BYTE)
    public int syncId;
    public int revision;
    public List<CommonTypes.ItemStack> slots;
    public Optional<CommonTypes.ItemStack> cursorStack;
}
