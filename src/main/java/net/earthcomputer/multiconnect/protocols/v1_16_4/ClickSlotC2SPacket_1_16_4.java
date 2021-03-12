package net.earthcomputer.multiconnect.protocols.v1_16_4;

import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.screen.slot.SlotActionType;

public class ClickSlotC2SPacket_1_16_4 implements Packet<ServerPlayPacketListener> {
    private final int syncId;
    private final int slotId;
    private final int clickData;
    private final short actionId;
    private final SlotActionType slotActionType;
    private final ItemStack slotItemBeforeModification;

    public ClickSlotC2SPacket_1_16_4(int syncId, int slotId, int clickData, SlotActionType slotActionType, ItemStack slotItemBeforeModification, short actionId) {
        this.syncId = syncId;
        this.slotId = slotId;
        this.clickData = clickData;
        this.slotActionType = slotActionType;
        this.slotItemBeforeModification = slotItemBeforeModification.copy();
        this.actionId = actionId;
    }

    public ClickSlotC2SPacket_1_16_4(PacketByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeByte(syncId);
        buf.writeShort(slotId);
        buf.writeByte(clickData);
        buf.writeShort(actionId);
        buf.writeEnumConstant(slotActionType);
        buf.writeItemStack(slotItemBeforeModification);
    }

    @Override
    public void apply(ServerPlayPacketListener listener) {
        throw new UnsupportedOperationException();
    }
}
