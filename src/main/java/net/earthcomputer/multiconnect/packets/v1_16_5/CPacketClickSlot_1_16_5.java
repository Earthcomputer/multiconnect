package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.packets.CPacketClickSlot;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.latest.CPacketClickSlot_Latest;
import net.earthcomputer.multiconnect.protocols.v1_16_5.Protocol_1_16_5;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

@MessageVariant(maxVersion = Protocols.V1_16_5)
public class CPacketClickSlot_1_16_5 implements CPacketClickSlot {
    public byte syncId;
    public short slotId;
    public byte clickData;
    public short actionId;
    public CPacketClickSlot_Latest.Mode mode;
    public CommonTypes.ItemStack slotItemBeforeModification;

    public static CPacketClickSlot_1_16_5 create(int syncId, int slotId, int clickData, SlotActionType actionType, ItemStack slotItemBeforeModification, int actionId) {
        CPacketClickSlot_1_16_5 newPacket = PacketSystem.defaultConstruct(CPacketClickSlot_1_16_5.class);
        newPacket.syncId = (byte) syncId;
        newPacket.slotId = (short) slotId;
        newPacket.clickData = (byte) clickData;
        newPacket.mode = CPacketClickSlot_Latest.Mode.FROM_MINECRAFT.coerce(actionType);
        newPacket.slotItemBeforeModification = CommonTypes.ItemStack.fromMinecraft(slotItemBeforeModification);
        newPacket.actionId = Protocol_1_16_5.nextScreenActionId();
        return newPacket;
    }
}
