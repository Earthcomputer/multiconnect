package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.EnumCoerce;
import net.earthcomputer.multiconnect.packets.CPacketContainerClick;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.minecraft.world.inventory.ClickType;
import java.util.List;

@MessageVariant(minVersion = Protocols.V1_17_1)
public class CPacketContainerClick_Latest implements CPacketContainerClick {
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

        public static final EnumCoerce<ClickType, Mode> FROM_MINECRAFT = new EnumCoerce<>(ClickType.class, Mode.class);
    }

    @MessageVariant
    public static class Slot {
        public short slot;
        public CommonTypes.ItemStack stack;
    }
}
