package net.earthcomputer.multiconnect.packets.v1_15_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketSetEquipment;

@MessageVariant(maxVersion = Protocols.V1_15_2)
public class SPacketSetEquipment_1_15_2 implements SPacketSetEquipment {
    public int entityId;
    public EquipmentSlot slot;
    public CommonTypes.ItemStack stack;

    @NetworkEnum
    public enum EquipmentSlot {
        MAINHAND,
        OFFHAND,
        FEET,
        LEGS,
        CHEST,
        HEAD,
    }
}