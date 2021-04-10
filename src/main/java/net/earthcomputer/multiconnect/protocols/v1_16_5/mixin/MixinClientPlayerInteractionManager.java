package net.earthcomputer.multiconnect.protocols.v1_16_5.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_16_5.ClickSlotC2SPacket_1_16_5;
import net.earthcomputer.multiconnect.protocols.v1_16_5.Protocol_1_16_5;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {
    @Shadow @Final private MinecraftClient client;
    private ItemStack oldCursorStack;
    private List<ItemStack> oldItems;

    @ModifyVariable(method = "clickSlot", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private List<ItemStack> captureOldItems(List<ItemStack> oldItems) {
        assert client.player != null;
        oldCursorStack = client.player.currentScreenHandler.getCursorStack().copy();
        return this.oldItems = oldItems;
    }

    @ModifyArg(method = "clickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"), index = 0)
    private Packet<?> modifySlotClickPacket(Packet<?> packet) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_16_5 && packet instanceof ClickSlotC2SPacket) {
            ClickSlotC2SPacket clickSlot = (ClickSlotC2SPacket) packet;

            ItemStack slotItemBeforeModification;
            if (clickSlot.getActionType() == SlotActionType.QUICK_CRAFT) {
                // Special case: quick craft always uses empty stack for verification
                slotItemBeforeModification = ItemStack.EMPTY;
            } else if (clickSlot.getActionType() == SlotActionType.QUICK_MOVE && ConnectionInfo.protocolVersion > Protocols.V1_11_2) {
                // Special case: quick move always uses empty stack for verification since 1.12
                slotItemBeforeModification = ItemStack.EMPTY;
            } else if (clickSlot.getActionType() == SlotActionType.PICKUP && clickSlot.getSlot() == -999) {
                // Special case: pickup with slot -999 (outside window) to throw items always uses empty stack for verification
                slotItemBeforeModification = ItemStack.EMPTY;
            } else if (clickSlot.getSlot() < 0 || clickSlot.getSlot() >= oldItems.size()) {
                slotItemBeforeModification = oldCursorStack;
            } else {
                slotItemBeforeModification = oldItems.get(clickSlot.getSlot());
            }

            packet = new ClickSlotC2SPacket_1_16_5(
                    clickSlot.getSyncId(),
                    clickSlot.getSlot(),
                    clickSlot.getButton(),
                    clickSlot.getActionType(),
                    slotItemBeforeModification,
                    Protocol_1_16_5.nextScreenActionId()
            );
        }

        oldCursorStack = null;
        oldItems = null;

        return packet;
    }
}
