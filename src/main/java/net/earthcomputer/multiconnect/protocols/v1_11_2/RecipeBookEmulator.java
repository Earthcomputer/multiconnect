package net.earthcomputer.multiconnect.protocols.v1_11_2;

import net.earthcomputer.multiconnect.protocols.v1_11_2.mixin.SlotAccessor;
import net.earthcomputer.multiconnect.protocols.v1_12.PlaceRecipeC2SPacket_1_12;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickWindowC2SPacket;
import net.minecraft.network.packet.s2c.play.ConfirmGuiActionS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RecipeBookEmulator {

    private final ScreenHandler screenHandler;

    private List<Pair<Short, Short>> recipeTransactionIdRanges = new ArrayList<>();

    public RecipeBookEmulator(ScreenHandler screenHandler) {
        this.screenHandler = screenHandler;
    }

    public void emulateRecipePlacement(PlaceRecipeC2SPacket_1_12 packet) {
        List<Pair<PlaceRecipeC2SPacket_1_12.Transaction, Integer>> transactionsFromMatrix = mergeTransactions(packet.getTransactionsFromMatrix());
        if (transactionsFromMatrix == null) {
            return;
        }
        List<Pair<PlaceRecipeC2SPacket_1_12.Transaction, Integer>> transactionsToMatrix = mergeTransactions(packet.getTransactionsToMatrix());
        if (transactionsToMatrix == null) {
            return;
        }

        short startTransactionId = ((IScreenHandler) screenHandler).multiconnect_getCurrentActionId();

        for (Pair<PlaceRecipeC2SPacket_1_12.Transaction, Integer> transaction : transactionsFromMatrix) {
            transfer(transaction.getLeft().craftingSlot, transaction.getLeft().invSlot, transaction.getLeft().stack.getCount() * transaction.getRight(), transaction.getLeft().originalStack);
        }
        for (Pair<PlaceRecipeC2SPacket_1_12.Transaction, Integer> transaction : transactionsToMatrix) {
            transfer(transaction.getLeft().invSlot, transaction.getLeft().craftingSlot, transaction.getLeft().stack.getCount() * transaction.getRight(), transaction.getLeft().originalStack);
        }

        recipeTransactionIdRanges.add(new Pair<>(startTransactionId, ((IScreenHandler) screenHandler).multiconnect_getCurrentActionId()));
    }

    public void onConfirmTransaction(ConfirmGuiActionS2CPacket packet) {
        short transactionId = packet.getActionId();

        Iterator<Pair<Short, Short>> itr = recipeTransactionIdRanges.iterator();
        while (itr.hasNext()) {
            Pair<Short, Short> range = itr.next();
            short min = range.getLeft();
            short max = range.getRight();
            boolean insideRange;
            if (min > max) {
                insideRange = transactionId >= min || transactionId <= max;
            } else {
                insideRange = transactionId >= min && transactionId <= max;
            }

            if (insideRange && !packet.wasAccepted()) {
                recipeTransactionIdRanges.clear();
                resyncContainer();
                return;
            } else if (transactionId == max) {
                itr.remove();
            }
        }
    }

    private List<Pair<PlaceRecipeC2SPacket_1_12.Transaction, Integer>> mergeTransactions(List<PlaceRecipeC2SPacket_1_12.Transaction> transactions) {
        // merge
        List<Pair<PlaceRecipeC2SPacket_1_12.Transaction, Integer>> merged = new ArrayList<>();
        for (PlaceRecipeC2SPacket_1_12.Transaction transaction : transactions) {
            boolean canMerge = false;
            if (!merged.isEmpty()) {
                PlaceRecipeC2SPacket_1_12.Transaction lastTransaction = merged.get(merged.size() - 1).getLeft();
                if (lastTransaction.stack.getCount() == transaction.stack.getCount()
                        && lastTransaction.craftingSlot == transaction.craftingSlot
                        && lastTransaction.invSlot == transaction.invSlot) {
                    canMerge = true;
                }
            }

            if (canMerge) {
                Pair<PlaceRecipeC2SPacket_1_12.Transaction, Integer> lastTransaction = merged.get(merged.size() - 1);
                merged.set(merged.size() - 1, new Pair<>(lastTransaction.getLeft(), lastTransaction.getRight() + 1));
            } else {
                merged.add(new Pair<>(transaction, 1));
            }
        }

        // translate inv slot to container slot
        for (int i = 0; i < merged.size(); i++) {
            Pair<PlaceRecipeC2SPacket_1_12.Transaction, Integer> transaction = merged.get(i);
            int slot = getInvSlot(transaction.getLeft().invSlot);
            if (slot == -1) {
                return null;
            }
            merged.set(i, new Pair<>(
                    new PlaceRecipeC2SPacket_1_12.Transaction(transaction.getLeft().originalStack, transaction.getLeft().stack, transaction.getLeft().craftingSlot, slot),
                    transaction.getRight()
            ));
        }

        return merged;
    }

    private int getInvSlot(int invSlot) {
        assert MinecraftClient.getInstance().player != null;
        PlayerInventory playerInv = MinecraftClient.getInstance().player.inventory;

        for (Slot slot : screenHandler.slots) {
            if (slot.inventory == playerInv && ((SlotAccessor) slot).getIndex() == invSlot) {
                return slot.id;
            }
        }
        return -1;
    }

    private void transfer(int fromSlot, int toSlot, int count, ItemStack clickedStack) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        assert player != null;

        // pickup (swap with cursor stack)
        player.networkHandler.sendPacket(new ClickWindowC2SPacket(screenHandler.syncId, fromSlot, 0, SlotActionType.PICKUP, clickedStack, screenHandler.getNextActionId(player.inventory)));

        // place items
        if (count == clickedStack.getCount()) {
            player.networkHandler.sendPacket(new ClickWindowC2SPacket(screenHandler.syncId, toSlot, 0, SlotActionType.PICKUP, ItemStack.EMPTY, screenHandler.getNextActionId(player.inventory)));
        } else {
            for (int i = 0; i < count; i++) {
                player.networkHandler.sendPacket(new ClickWindowC2SPacket(screenHandler.syncId, toSlot, 1, SlotActionType.PICKUP, ItemStack.EMPTY, screenHandler.getNextActionId(player.inventory)));
            }
        }

        // return (pickup old cursor stack)
        player.networkHandler.sendPacket(new ClickWindowC2SPacket(screenHandler.syncId, fromSlot, 0, SlotActionType.PICKUP, player.inventory.getCursorStack(), screenHandler.getNextActionId(player.inventory)));
    }

    private void resyncContainer() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        assert player != null;
        ClientPlayerInteractionManager interactionManager = MinecraftClient.getInstance().interactionManager;
        assert interactionManager != null;

        for (Slot slot : screenHandler.slots) {
            // If the slot was correctly synced, will swap the cursor stack with the stack in that slot, and back again.
            // If the slot was desynced or the container is locked due to a previous desync, the swap will be denied,
            // and the server will require the client to acknowledge the new item. Since that acknowledgement is
            // processed on the same thread as this code, it cannot happen in between, so the second packet will also
            // be sent before the acknowledgement, so it will also be rejected.
            interactionManager.clickSlot(screenHandler.syncId, slot.id, 0, SlotActionType.PICKUP, player);
            interactionManager.clickSlot(screenHandler.syncId, slot.id, 0, SlotActionType.PICKUP, player);
        }
    }

}
