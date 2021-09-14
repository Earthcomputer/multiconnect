package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;

import java.util.List;

@Message
public class CPacketClickSlot {
    @Type(Types.UNSIGNED_BYTE)
    public int syncId;
    public int revision;
    public short slot;
    public byte button;
    public Mode mode;
    public List<Slot> slots;
    public CommonTypes.ItemStack clickedItem;

    public enum Mode {
        PICKUP, QUICK_MOVE, SWAP, CLONE, THROW, QUICK_CRAFT, PICKUP_ALL
    }

    @Message
    public static class Slot {
        public short slot;
        public CommonTypes.ItemStack stack;
    }
}
