package net.earthcomputer.multiconnect.protocols.v1_11_2;

import net.earthcomputer.multiconnect.protocols.v1_12.PlaceRecipeC2SPacket_1_12;
import net.earthcomputer.multiconnect.protocols.v1_16_5.AckScreenActionS2CPacket_1_16_5;
import net.earthcomputer.multiconnect.protocols.v1_16_5.ClickSlotC2SPacket_1_16_5;
import net.earthcomputer.multiconnect.protocols.v1_16_5.Protocol_1_16_5;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class RecipeBookEmulator {

    private final ScreenHandler screenHandler;

    private final List<Pair<Short, Short>> recipeTransactionIdRanges = new ArrayList<>();

    public RecipeBookEmulator(ScreenHandler screenHandler) {
        this.screenHandler = screenHandler;
    }

    public static void setCraftingResultSlot(int syncId, ScreenHandler screenHandler, CraftingInventory craftingInv) {
        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        assert networkHandler != null;
        ItemStack result = networkHandler.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, craftingInv, MinecraftClient.getInstance().world)
                .map(recipe -> recipe.craft(craftingInv))
                .orElse(ItemStack.EMPTY);
        networkHandler.onScreenHandlerSlotUpdate(new ScreenHandlerSlotUpdateS2CPacket(syncId, screenHandler.getRevision(), 0, result));
    }

    public void emulateRecipePlacement(PlaceRecipeC2SPacket_1_12 packet) {
        var transactionsFromMatrix = mergeTransactions(packet.getTransactionsFromMatrix());
        if (transactionsFromMatrix == null) {
            return;
        }
        var transactionsToMatrix = mergeTransactions(packet.getTransactionsToMatrix());
        if (transactionsToMatrix == null) {
            return;
        }

        short startTransactionId = Protocol_1_16_5.getLastScreenActionId();

        for (var transaction : transactionsFromMatrix) {
            transfer(transaction.getLeft().craftingSlot, transaction.getLeft().invSlot,
                    transaction.getLeft().stack.getCount() * transaction.getRight(), transaction.getLeft().placedOn,
                    transaction.getLeft().originalStack);
        }
        for (var transaction : transactionsToMatrix) {
            transfer(transaction.getLeft().invSlot, transaction.getLeft().craftingSlot,
                    transaction.getLeft().stack.getCount() * transaction.getRight(), transaction.getLeft().placedOn,
                    transaction.getLeft().originalStack);
        }

        recipeTransactionIdRanges.add(new Pair<>(startTransactionId, Protocol_1_16_5.getLastScreenActionId()));
    }

    public void onAckScreenAction(AckScreenActionS2CPacket_1_16_5 packet) {
        short transactionId = packet.getActionId();

        var itr = recipeTransactionIdRanges.iterator();
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
        var merged = new ArrayList<Pair<PlaceRecipeC2SPacket_1_12.Transaction, Integer>>();
        for (var transaction : transactions) {
            boolean canMerge = false;
            if (!merged.isEmpty()) {
                var lastTransaction = merged.get(merged.size() - 1).getLeft();
                if (lastTransaction.stack.getCount() == transaction.stack.getCount()
                        && lastTransaction.craftingSlot == transaction.craftingSlot
                        && lastTransaction.invSlot == transaction.invSlot) {
                    canMerge = true;
                }
            }

            if (canMerge) {
                var lastTransaction = merged.get(merged.size() - 1);
                merged.set(merged.size() - 1, new Pair<>(lastTransaction.getLeft(), lastTransaction.getRight() + 1));
            } else {
                merged.add(new Pair<>(transaction, 1));
            }
        }

        // translate inv slot to container slot
        for (int i = 0; i < merged.size(); i++) {
            var transaction = merged.get(i);
            var firstTransaction = transaction.getLeft();
            int slot = getInvSlot(firstTransaction.invSlot);
            if (slot == -1) {
                return null;
            }
            merged.set(i, new Pair<>(
                    new PlaceRecipeC2SPacket_1_12.Transaction(firstTransaction.originalStack, firstTransaction.stack,
                            firstTransaction.placedOn, firstTransaction.craftingSlot, slot),
                    transaction.getRight()
            ));
        }

        return merged;
    }

    private int getInvSlot(int invSlot) {
        assert MinecraftClient.getInstance().player != null;
        PlayerInventory playerInv = MinecraftClient.getInstance().player.getInventory();

        for (Slot slot : screenHandler.slots) {
            if (slot.inventory == playerInv && slot.getIndex() == invSlot) {
                return slot.id;
            }
        }
        return -1;
    }

    private void transfer(int fromSlot, int toSlot, int count, ItemStack placedOn, ItemStack clickedStack) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        assert player != null;

        // pickup (swap with cursor stack)
        player.networkHandler.sendPacket(new ClickSlotC2SPacket_1_16_5(screenHandler.syncId, fromSlot, 0, SlotActionType.PICKUP, clickedStack, Protocol_1_16_5.nextScreenActionId()));

        // place items
        if (count == clickedStack.getCount()) {
            player.networkHandler.sendPacket(new ClickSlotC2SPacket_1_16_5(screenHandler.syncId, toSlot, 0, SlotActionType.PICKUP, placedOn, Protocol_1_16_5.nextScreenActionId()));
        } else {
            for (int i = 0; i < count; i++) {
                ItemStack existingStack = clickedStack.copy();
                existingStack.setCount(placedOn.getCount() + i);
                player.networkHandler.sendPacket(new ClickSlotC2SPacket_1_16_5(screenHandler.syncId, toSlot, 1, SlotActionType.PICKUP, existingStack, Protocol_1_16_5.nextScreenActionId()));
            }
        }

        // return (pickup old cursor stack)
        player.networkHandler.sendPacket(new ClickSlotC2SPacket_1_16_5(screenHandler.syncId, fromSlot, 0, SlotActionType.PICKUP, screenHandler.getCursorStack(), Protocol_1_16_5.nextScreenActionId()));
    }

    private void resyncContainer() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        assert player != null;
        var interactionManager = MinecraftClient.getInstance().interactionManager;
        assert interactionManager != null;

        int craftingResultSlotId = screenHandler instanceof AbstractRecipeScreenHandler ?
                ((AbstractRecipeScreenHandler<?>) screenHandler).getCraftingResultSlotIndex() : -1;

        for (Slot slot : screenHandler.slots) {
            if (slot.id == craftingResultSlotId) {
                continue;
            }
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
