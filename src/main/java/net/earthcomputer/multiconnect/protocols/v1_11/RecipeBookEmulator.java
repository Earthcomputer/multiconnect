package net.earthcomputer.multiconnect.protocols.v1_11;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.packets.v1_16_5.CPacketContainerClick_1_16_5;
import net.earthcomputer.multiconnect.protocols.v1_16.Protocol_1_16_5;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.List;

public class RecipeBookEmulator {

    private final AbstractContainerMenu menu;

    private final List<Tuple<Short, Short>> recipeTransactionIdRanges = new ArrayList<>();

    public RecipeBookEmulator(AbstractContainerMenu menu) {
        this.menu = menu;
    }

    public static void setCraftingResultSlot(int syncId, AbstractContainerMenu menu, CraftingContainer craftingInv) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        assert connection != null;
        ItemStack result = connection.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftingInv, Minecraft.getInstance().level)
                .map(recipe -> recipe.assemble(craftingInv))
                .orElse(ItemStack.EMPTY);
        connection.handleContainerSetSlot(new ClientboundContainerSetSlotPacket(syncId, menu.getStateId(), 0, result));
    }

    // TODO: rewrite 1.12
//    public void emulateRecipePlacement(PlaceRecipeC2SPacket_1_12 packet) {
//        var transactionsFromMatrix = mergeTransactions(packet.getTransactionsFromMatrix());
//        if (transactionsFromMatrix == null) {
//            return;
//        }
//        var transactionsToMatrix = mergeTransactions(packet.getTransactionsToMatrix());
//        if (transactionsToMatrix == null) {
//            return;
//        }
//
//        short startTransactionId = Protocol_1_16_5.getLastScreenActionId();
//
//        for (var transaction : transactionsFromMatrix) {
//            transfer(transaction.getLeft().craftingSlot, transaction.getLeft().invSlot,
//                    transaction.getLeft().stack.getCount() * transaction.getRight(), transaction.getLeft().placedOn,
//                    transaction.getLeft().originalStack);
//        }
//        for (var transaction : transactionsToMatrix) {
//            transfer(transaction.getLeft().invSlot, transaction.getLeft().craftingSlot,
//                    transaction.getLeft().stack.getCount() * transaction.getRight(), transaction.getLeft().placedOn,
//                    transaction.getLeft().originalStack);
//        }
//
//        recipeTransactionIdRanges.add(new Pair<>(startTransactionId, Protocol_1_16_5.getLastScreenActionId()));
//    }

    public void onAckScreenAction(short transactionId, boolean wasAccepted) {
        var itr = recipeTransactionIdRanges.iterator();
        while (itr.hasNext()) {
            Tuple<Short, Short> range = itr.next();
            short min = range.getA();
            short max = range.getB();
            boolean insideRange;
            if (min > max) {
                insideRange = transactionId >= min || transactionId <= max;
            } else {
                insideRange = transactionId >= min && transactionId <= max;
            }

            if (insideRange && !wasAccepted) {
                recipeTransactionIdRanges.clear();
                resyncContainer();
                return;
            } else if (transactionId == max) {
                itr.remove();
            }
        }
    }

    // TODO: rewrite 1.12
//    private List<Pair<PlaceRecipeC2SPacket_1_12.Transaction, Integer>> mergeTransactions(List<PlaceRecipeC2SPacket_1_12.Transaction> transactions) {
//        // merge
//        var merged = new ArrayList<Pair<PlaceRecipeC2SPacket_1_12.Transaction, Integer>>();
//        for (var transaction : transactions) {
//            boolean canMerge = false;
//            if (!merged.isEmpty()) {
//                var lastTransaction = merged.get(merged.size() - 1).getLeft();
//                if (lastTransaction.stack.getCount() == transaction.stack.getCount()
//                        && lastTransaction.craftingSlot == transaction.craftingSlot
//                        && lastTransaction.invSlot == transaction.invSlot) {
//                    canMerge = true;
//                }
//            }
//
//            if (canMerge) {
//                var lastTransaction = merged.get(merged.size() - 1);
//                merged.set(merged.size() - 1, new Pair<>(lastTransaction.getLeft(), lastTransaction.getRight() + 1));
//            } else {
//                merged.add(new Pair<>(transaction, 1));
//            }
//        }
//
//        // translate inv slot to container slot
//        for (int i = 0; i < merged.size(); i++) {
//            var transaction = merged.get(i);
//            var firstTransaction = transaction.getLeft();
//            int slot = getInvSlot(firstTransaction.invSlot);
//            if (slot == -1) {
//                return null;
//            }
//            merged.set(i, new Pair<>(
//                    new PlaceRecipeC2SPacket_1_12.Transaction(firstTransaction.originalStack, firstTransaction.stack,
//                            firstTransaction.placedOn, firstTransaction.craftingSlot, slot),
//                    transaction.getRight()
//            ));
//        }
//
//        return merged;
//    }

    private int getInvSlot(int invSlot) {
        assert Minecraft.getInstance().player != null;
        Inventory playerInv = Minecraft.getInstance().player.getInventory();

        for (Slot slot : menu.slots) {
            if (slot.container == playerInv && slot.getContainerSlot() == invSlot) {
                return slot.index;
            }
        }
        return -1;
    }

    private void transfer(int fromSlot, int toSlot, int count, ItemStack placedOn, ItemStack clickedStack) {
        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;

        // pickup (swap with cursor stack)
        PacketSystem.sendToServer(player.connection, Protocols.V1_16_5, CPacketContainerClick_1_16_5.create(menu.containerId, fromSlot, 0, ClickType.PICKUP, clickedStack, Protocol_1_16_5.nextScreenActionId()));

        // place items
        if (count == clickedStack.getCount()) {
            PacketSystem.sendToServer(player.connection, Protocols.V1_16_5, CPacketContainerClick_1_16_5.create(menu.containerId, toSlot, 0, ClickType.PICKUP, placedOn, Protocol_1_16_5.nextScreenActionId()));
        } else {
            for (int i = 0; i < count; i++) {
                ItemStack existingStack = clickedStack.copy();
                existingStack.setCount(placedOn.getCount() + i);
                PacketSystem.sendToServer(player.connection, Protocols.V1_16_5, CPacketContainerClick_1_16_5.create(menu.containerId, toSlot, 1, ClickType.PICKUP, existingStack, Protocol_1_16_5.nextScreenActionId()));
            }
        }

        // return (pickup old cursor stack)
        PacketSystem.sendToServer(player.connection, Protocols.V1_16_5, CPacketContainerClick_1_16_5.create(menu.containerId, fromSlot, 0, ClickType.PICKUP, menu.getCarried(), Protocol_1_16_5.nextScreenActionId()));
    }

    private void resyncContainer() {
        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        var interactionManager = Minecraft.getInstance().gameMode;
        assert interactionManager != null;

        int craftingResultSlotId = menu instanceof RecipeBookMenu ?
                ((RecipeBookMenu<?>) menu).getResultSlotIndex() : -1;

        for (Slot slot : menu.slots) {
            if (slot.index == craftingResultSlotId) {
                continue;
            }
            // If the slot was correctly synced, will swap the cursor stack with the stack in that slot, and back again.
            // If the slot was desynced or the container is locked due to a previous desync, the swap will be denied,
            // and the server will require the client to acknowledge the new item. Since that acknowledgement is
            // processed on the same thread as this code, it cannot happen in between, so the second packet will also
            // be sent before the acknowledgement, so it will also be rejected.
            interactionManager.handleInventoryMouseClick(menu.containerId, slot.index, 0, ClickType.PICKUP, player);
            interactionManager.handleInventoryMouseClick(menu.containerId, slot.index, 0, ClickType.PICKUP, player);
        }
    }

}
