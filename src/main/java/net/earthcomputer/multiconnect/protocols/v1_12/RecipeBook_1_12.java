package net.earthcomputer.multiconnect.protocols.v1_12;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.packets.v1_12.CPacketPlaceRecipe_1_12;
import net.earthcomputer.multiconnect.packets.v1_12_2.ItemStack_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_16_5.Protocol_1_16_5;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class RecipeBook_1_12<C extends Inventory> {

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final RecipeBookWidget recipeBookWidget;
    private final IRecipeBookWidget iRecipeBookWidget;
    private final AbstractRecipeScreenHandler<C> screenHandler;

    public RecipeBook_1_12(RecipeBookWidget recipeBookWidget, IRecipeBookWidget iRecipeBookWidget, AbstractRecipeScreenHandler<C> screenHandler) {
        this.recipeBookWidget = recipeBookWidget;
        this.iRecipeBookWidget = iRecipeBookWidget;
        this.screenHandler = screenHandler;
    }

    public void handleRecipeClicked(Recipe<C> recipe, RecipeResultCollection recipes) {
        assert mc.player != null;
        assert mc.getNetworkHandler() != null;

        boolean craftable = recipes.isCraftable(recipe);

        if (!craftable && iRecipeBookWidget.getGhostSlots().getRecipe() == recipe) {
            return;
        }

        if (!canClearCraftMatrix() && !mc.player.isCreative()) {
            return;
        }

        if (craftable) {
            tryPlaceRecipe(recipe, screenHandler.slots);
        } else {
            // clear craft matrix and show ghost recipe
            var transactionsFromMatrix = clearCraftMatrix();
            recipeBookWidget.showGhostRecipe(recipe, screenHandler.slots);

            if (!transactionsFromMatrix.isEmpty()) {
                var packet = new CPacketPlaceRecipe_1_12();
                packet.syncId = screenHandler.syncId;
                packet.transactionId = Protocol_1_16_5.nextScreenActionId();
                packet.transactionsToMatrix = new ArrayList<>();
                packet.transactionsFromMatrix = transactionsFromMatrix.stream().map(Transaction::toPacketTransaction).collect(Collectors.toCollection(ArrayList::new));
                PacketSystem.sendToServer(mc.getNetworkHandler(), Protocols.V1_12, packet);

                if (iRecipeBookWidget.getRecipeBook().isFilteringCraftable(screenHandler)) {
                    mc.player.getInventory().markDirty();
                }
            }
        }

        if (!iRecipeBookWidget.multiconnect_isWide()) {
            recipeBookWidget.toggleOpen();
        }
    }

    private void tryPlaceRecipe(Recipe<C> recipe, List<Slot> slots) {
        assert mc.player != null;
        assert mc.getNetworkHandler() != null;

        boolean alreadyPlaced = screenHandler.matches(recipe);
        int possibleCraftCount = iRecipeBookWidget.getRecipeFinder().countCrafts(recipe, null);

        if (alreadyPlaced) {
            // check each item in the input to see if we're already at the max crafts possible
            boolean canPlaceMore = false;

            for (int i = 0; i < screenHandler.getCraftingSlotCount(); i++) {
                if (!isPartOfCraftMatrix(i))
                    continue;

                ItemStack stack = screenHandler.getSlot(i).getStack();

                if (!stack.isEmpty() && stack.getCount() < possibleCraftCount) {
                    canPlaceMore = true;
                }
            }

            if (!canPlaceMore) {
                return;
            }
        }

        int craftCount = calcCraftCount(possibleCraftCount, alreadyPlaced);

        IntList inputItemIds = new IntArrayList();
        if (iRecipeBookWidget.getRecipeFinder().match(recipe, inputItemIds, craftCount)) {
            // take into account max stack sizes now we've found the actual inputs
            int actualCount = craftCount;

            for (int itemId : inputItemIds) {
                int maxCount = RecipeMatcher.getStackFromId(itemId).getMaxCount();

                if (actualCount > maxCount) {
                    actualCount = maxCount;
                }
            }

            if (iRecipeBookWidget.getRecipeFinder().match(recipe, inputItemIds, actualCount)) {
                // clear the craft matrix and place the recipe
                var transactionsFromMatrix = clearCraftMatrix();
                var transactionsToMatrix = new ArrayList<Transaction>();
                placeRecipe(recipe, slots, actualCount, inputItemIds, transactionsToMatrix);
                var packet = new CPacketPlaceRecipe_1_12();
                packet.syncId = screenHandler.syncId;
                packet.transactionId = Protocol_1_16_5.nextScreenActionId();
                packet.transactionsToMatrix = transactionsToMatrix.stream().map(Transaction::toPacketTransaction).collect(Collectors.toCollection(ArrayList::new));
                packet.transactionsFromMatrix = transactionsFromMatrix.stream().map(Transaction::toPacketTransaction).collect(Collectors.toCollection(ArrayList::new));
                PacketSystem.sendToServer(mc.getNetworkHandler(), Protocols.V1_12, packet);
            }
        }
    }

    private List<Transaction> clearCraftMatrix() {
        assert mc.player != null;

        iRecipeBookWidget.getGhostSlots().reset();
        PlayerInventory playerInv = mc.player.getInventory();
        var transactionsFromMatrix = new ArrayList<Transaction>();

        int serverSlot = 1;
        for (int i = 0; i < screenHandler.getCraftingSlotCount(); i++) {
            if (!isPartOfCraftMatrix(i)) continue;

            ItemStack stack = screenHandler.getSlot(i).getStack();

            if (!stack.isEmpty()) {
                while (stack.getCount() > 0) {
                    int destSlot = getOccupiedSlotWithRoomForStack(playerInv, stack);

                    if (destSlot == -1) {
                        destSlot = playerInv.getEmptySlot();
                    }

                    if (destSlot == -1) {
                        // Wtf, this happens?!
                        break;
                    }

                    ItemStack originalStack = stack.copy();
                    ItemStack targetStack = stack.copy();
                    targetStack.setCount(1);
                    ItemStack placedOn = playerInv.getStack(destSlot).copy();

                    if (playerInv.insertStack(destSlot, targetStack)) {
                        targetStack.increment(1);
                    } else {
                        // This shouldn't happen - condition already checked by canClearCraftMatrix
                        LogManager.getLogger().error("Can't find any space for item in inventory");
                    }

                    screenHandler.getSlot(i).takeStack(1);
                    transactionsFromMatrix.add(new Transaction(originalStack,
                            targetStack.copy(), placedOn, serverSlot, destSlot));
                }
            }

            serverSlot++;
        }

        screenHandler.clearCraftingSlots();

        return transactionsFromMatrix;
    }

    private int calcCraftCount(int possibleCraftCount, boolean alreadyPlaced) {
        int stackSize = 1;

        if (Screen.hasShiftDown()) {
            // craft all
            stackSize = possibleCraftCount;
        } else if (alreadyPlaced) {
            // craft single, find the item already in place with the minimum count and add one more craft than that
            stackSize = 64;
            for (int i = 0; i < screenHandler.getCraftingSlotCount(); i++) {
                if (!isPartOfCraftMatrix(i)) continue;

                ItemStack stack = screenHandler.getSlot(i).getStack();
                if (!stack.isEmpty() && stack.getCount() < stackSize) {
                    stackSize = stack.getCount();
                }
            }

            if (stackSize < 64) {
                stackSize++;
            }
        }

        return stackSize;
    }

    private void placeRecipe(Recipe<C> recipe, List<Slot> slots, int placeCount, IntList inputItemIds, List<Transaction> transactionsToMatrix) {
        int width = screenHandler.getCraftingWidth();
        int height = screenHandler.getCraftingHeight();

        if (recipe instanceof ShapedRecipe shaped) {
            width = shaped.getWidth();
            height = shaped.getHeight();
        }

        int serverSlot;
        for (serverSlot = 0; !isPartOfCraftMatrix(serverSlot); serverSlot++)
            ;

        Iterator<Integer> inputItemItr = inputItemIds.iterator();

        // :thonkjang: probably meant to swap craftingWidth and craftingHeight here, but oh well because width = height
        for (int y = 0; y < screenHandler.getCraftingWidth() && y != height; y++) {
            for (int x = 0; x < screenHandler.getCraftingHeight(); x++) {
                if (x == width || !inputItemItr.hasNext()) {
                    serverSlot += screenHandler.getCraftingWidth() - x;
                    break;
                }

                Slot slot = slots.get(serverSlot);

                ItemStack stackNeeded = RecipeMatcher.getStackFromId(inputItemItr.next());
                if (!stackNeeded.isEmpty()) {
                    for (int i = 0; i < placeCount; i++) {
                        var transaction = findAndMoveToCraftMatrix(serverSlot, slot, stackNeeded);
                        if (transaction != null) {
                            transactionsToMatrix.add(transaction);
                        }
                    }
                }
                serverSlot++;
            }

            if (!inputItemItr.hasNext()) {
                break;
            }
        }
    }

    private Transaction findAndMoveToCraftMatrix(int destSlotIndex, Slot destSlot, ItemStack stackNeeded) {
        assert mc.player != null;

        PlayerInventory playerInv = mc.player.getInventory();
        int fromSlot = playerInv.indexOf(stackNeeded);

        if (fromSlot == -1) {
            return null;
        } else {
            ItemStack stack = playerInv.getStack(fromSlot).copy();

            if (stack.isEmpty()) {
                LogManager.getLogger().error("Matched: " + stackNeeded.getTranslationKey() + " with empty item.");
                return null;
            } else {
                if (stack.getCount() > 1) {
                    playerInv.removeStack(fromSlot, 1);
                } else {
                    playerInv.removeStack(fromSlot);
                }

                ItemStack originalStack = stack.copy();
                stack.setCount(1);
                ItemStack placedOn = destSlot.getStack().copy();

                if (destSlot.getStack().isEmpty()) {
                    destSlot.setStack(stack);
                } else {
                    destSlot.getStack().increment(1);
                }

                return new Transaction(originalStack, stack, placedOn, destSlotIndex, fromSlot);
            }
        }
    }

    private boolean canClearCraftMatrix() {
        assert mc.player != null;

        PlayerInventory invPlayer = mc.player.getInventory();

        for (int i = 0; i < screenHandler.getCraftingSlotCount(); ++i) {
            if (!isPartOfCraftMatrix(i)) continue;

            ItemStack stack = screenHandler.getSlot(i).getStack();

            if (!stack.isEmpty()) {
                int destStack = getOccupiedSlotWithRoomForStack(invPlayer, stack);

                if (destStack == -1) {
                    destStack = invPlayer.getEmptySlot();
                }

                if (destStack == -1) {
                    return false;
                }
            }
        }

        return true;
    }

    private int getOccupiedSlotWithRoomForStack(PlayerInventory playerInv, ItemStack stack) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_11_2) {
            if (canStackAddMore(playerInv.getStack(playerInv.selectedSlot), stack)) {
                return playerInv.selectedSlot;
            }
            for (int j = 0; j < playerInv.main.size(); j++) {
                if (canStackAddMore(playerInv.main.get(j), stack)) {
                    return j;
                }
            }
            return -1;
        } else {
            return playerInv.getOccupiedSlotWithRoomForStack(stack);
        }
    }

    private boolean canStackAddMore(ItemStack existingStack, ItemStack stack) {
        return !existingStack.isEmpty()
                && existingStack.getItem() == stack.getItem()
                && ItemStack.areNbtEqual(existingStack, stack)
                && existingStack.isStackable()
                && existingStack.getCount() < existingStack.getMaxCount()
                && existingStack.getCount() < 64;
    }

    private boolean isPartOfCraftMatrix(int slotId) {
        if (screenHandler instanceof AbstractFurnaceScreenHandler && slotId == 1) {
            // exclude fuel slot
            return false;
        }
        return slotId < screenHandler.getCraftingSlotCount() && slotId != screenHandler.getCraftingResultSlotIndex();
    }

    private record Transaction(
            ItemStack originalStack,
            ItemStack stack,
            ItemStack placedOn,
            int craftingSlot,
            int invSlot
    ) {
        private Transaction(ItemStack originalStack, ItemStack stack, ItemStack placedOn, int craftingSlot, int invSlot) {
            this.originalStack = originalStack;
            this.stack = stack.copy();
            this.placedOn = placedOn;
            this.craftingSlot = craftingSlot;
            this.invSlot = invSlot;
        }

        private CPacketPlaceRecipe_1_12.Transaction toPacketTransaction() {
            var result = new CPacketPlaceRecipe_1_12.Transaction();
            result.stack = ItemStack_1_12_2.fromMinecraft(this.stack);
            result.craftingSlot = (byte) this.craftingSlot;
            result.invSlot = (byte) this.invSlot;
            return result;
        }
    }
}
