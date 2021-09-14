package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.EnumCoerce;
import net.minecraft.screen.slot.SlotActionType;

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

    @Handler(protocol = Protocols.V1_16_5)
    public static void drop() {
    }

    public enum Mode {
        PICKUP, QUICK_MOVE, SWAP, CLONE, THROW, QUICK_CRAFT, PICKUP_ALL;

        public static final EnumCoerce<SlotActionType, Mode> FROM_MINECRAFT = new EnumCoerce<>(SlotActionType.class, Mode.class);
    }

    @Message
    public static class Slot {
        public short slot;
        public CommonTypes.ItemStack stack;
    }
}
