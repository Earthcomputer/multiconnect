package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.OnlyIf;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketSetEquipment;
import net.earthcomputer.multiconnect.packets.v1_15_2.SPacketSetEquipment_1_15_2;

@MessageVariant(minVersion = Protocols.V1_16)
public class SPacketSetEquipment_Latest implements SPacketSetEquipment {
    public int entityId;
    @Introduce(compute = "computeEntry")
    public Entry firstEntry;

    public static Entry computeEntry(
            @Argument("slot") SPacketSetEquipment_1_15_2.EquipmentSlot slot,
            @Argument(value = "stack", translate = true) CommonTypes.ItemStack stack
    ) {
        Entry entry = new Entry();
        entry.slot = (byte) slot.ordinal();
        entry.stack = stack;
        return entry;
    }

    @MessageVariant(tailrec = true)
    public static class Entry {
        public byte slot;
        public CommonTypes.ItemStack stack;
        @OnlyIf("hasNextEntry")
        public Entry nextEntry;

        public static boolean hasNextEntry(@Argument("slot") byte slot) {
            return (slot & 0x80) != 0;
        }
    }
}
