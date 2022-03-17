package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.EnumCoerce;
import net.earthcomputer.multiconnect.packets.CPacketClickSlot;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.minecraft.screen.slot.SlotActionType;

import java.util.List;

@MessageVariant(minVersion = Protocols.V1_17_1)
public class CPacketClickSlot_Latest implements CPacketClickSlot {
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

    @NetworkEnum
    public enum Mode {
        PICKUP, QUICK_MOVE, SWAP, CLONE, THROW, QUICK_CRAFT, PICKUP_ALL;

        public static final EnumCoerce<SlotActionType, Mode> FROM_MINECRAFT = new EnumCoerce<>(SlotActionType.class, Mode.class);
    }

    @MessageVariant
    public static class Slot {
        public short slot;
        public CommonTypes.ItemStack stack;
    }
}
