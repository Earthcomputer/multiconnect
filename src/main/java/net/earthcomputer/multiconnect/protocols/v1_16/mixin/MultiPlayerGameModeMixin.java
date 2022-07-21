package net.earthcomputer.multiconnect.protocols.v1_16.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.packets.v1_16_5.CPacketContainerClick_1_16_5;
import net.earthcomputer.multiconnect.protocols.v1_16.Protocol_1_16_5;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

// priority 0 fixes incompatibility with itemscroller
@Mixin(value = MultiPlayerGameMode.class, priority = 0)
public class MultiPlayerGameModeMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private ClientPacketListener connection;
    @Unique
    private ItemStack multiconnect_oldCursorStack;
    @Unique
    private List<ItemStack> multiconnect_oldItems;

    // TODO: reimplement for 1.19
//    @Redirect(method = "interactItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V", ordinal = 0),
//            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;syncSelectedSlot()V"), to = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V", ordinal = 1)))
//    private void redirectPlayerPosPacket(ClientPlayNetworkHandler clientPlayNetworkHandler, Packet<?> packet) {
//        if(ConnectionInfo.protocolVersion >= Protocols.V1_17){
//            clientPlayNetworkHandler.sendPacket(packet);
//        }
//    }

    @ModifyVariable(method = "handleInventoryMouseClick", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private List<ItemStack> captureOldItems(List<ItemStack> oldItems) {
        assert minecraft.player != null;
        multiconnect_oldCursorStack = minecraft.player.containerMenu.getCarried().copy();
        return this.multiconnect_oldItems = oldItems;
    }

    @Inject(method = "handleInventoryMouseClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"), cancellable = true)
    private void modifySlotClickPacket(int syncId, int slotId, int button, ClickType actionType, Player player, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_16_5) {
            ItemStack slotItemBeforeModification;
            if (actionType == ClickType.QUICK_CRAFT) {
                // Special case: quick craft always uses empty stack for verification
                slotItemBeforeModification = ItemStack.EMPTY;
            } else if (actionType == ClickType.QUICK_MOVE && ConnectionInfo.protocolVersion > Protocols.V1_11_2) {
                // Special case: quick move always uses empty stack for verification since 1.12
                slotItemBeforeModification = ItemStack.EMPTY;
            } else if (actionType == ClickType.PICKUP && slotId == -999) {
                // Special case: pickup with slot -999 (outside window) to throw items always uses empty stack for verification
                slotItemBeforeModification = ItemStack.EMPTY;
            } else if (slotId < 0 || slotId >= multiconnect_oldItems.size()) {
                slotItemBeforeModification = multiconnect_oldCursorStack;
            } else {
                slotItemBeforeModification = multiconnect_oldItems.get(slotId);
            }

            CPacketContainerClick_1_16_5 newPacket = CPacketContainerClick_1_16_5.create(syncId, slotId, button, actionType, slotItemBeforeModification, Protocol_1_16_5.nextScreenActionId());
            PacketSystem.sendToServer(this.connection, Protocols.V1_16_5, newPacket);
            ci.cancel();
        }

        multiconnect_oldCursorStack = null;
        multiconnect_oldItems = null;
    }
}
