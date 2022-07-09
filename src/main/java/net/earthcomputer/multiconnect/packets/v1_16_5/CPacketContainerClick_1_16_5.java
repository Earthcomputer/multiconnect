package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.ExplicitConstructible;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Sendable;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.packets.CPacketContainerClick;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.latest.CPacketContainerClick_Latest;
import net.earthcomputer.multiconnect.protocols.v1_16.Protocol_1_16_5;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

@MessageVariant(maxVersion = Protocols.V1_16_5)
@Sendable(from = Protocols.V1_16_5)
@ExplicitConstructible
public class CPacketContainerClick_1_16_5 implements CPacketContainerClick {
    public byte syncId;
    public short slot;
    public byte button;
    @Introduce(intValue = 0)
    public short actionId;
    public CPacketContainerClick_Latest.Mode mode;
    @Introduce(defaultConstruct = true)
    public CommonTypes.ItemStack slotItemBeforeModification;

    public static CPacketContainerClick_1_16_5 create(int syncId, int slotId, int clickData, ClickType actionType, ItemStack slotItemBeforeModification, int actionId) {
        CPacketContainerClick_1_16_5 newPacket = PacketSystem.defaultConstruct(CPacketContainerClick_1_16_5.class);
        newPacket.syncId = (byte) syncId;
        newPacket.slot = (short) slotId;
        newPacket.button = (byte) clickData;
        newPacket.mode = CPacketContainerClick_Latest.Mode.FROM_MINECRAFT.coerce(actionType);
        newPacket.slotItemBeforeModification = CommonTypes.ItemStack.fromMinecraft(slotItemBeforeModification);
        newPacket.actionId = Protocol_1_16_5.nextScreenActionId();
        return newPacket;
    }
}
