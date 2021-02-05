package net.earthcomputer.multiconnect.protocols.v1_12;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RecipeBook_1_12<C extends Inventory> {

    private MinecraftClient mc = MinecraftClient.getInstance();
    private RecipeBookWidget recipeBookWidget;
    private IRecipeBookWidget iRecipeBookWidget;
    private AbstractRecipeScreenHandler<C> container;

    public RecipeBook_1_12(RecipeBookWidget recipeBookWidget, IRecipeBookWidget iRecipeBookWidget, AbstractRecipeScreenHandler<C> container) {
        this.recipeBookWidget = recipeBookWidget;
        this.iRecipeBookWidget = iRecipeBookWidget;
        this.container = container;
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
            tryPlaceRecipe(recipe, container.slots);
        } else {
            // clear craft matrix and show ghost recipe
            List<PlaceRecipeC2SPacket_1_12.Transaction> transactionFromMatrix = clearCraftMatrix();
            recipeBookWidget.showGhostRecipe(recipe, container.slots);

            if (!transactionFromMatrix.isEmpty()) {
                short transactionId = mc.player.currentScreenHandler.getNextActionId(mc.player.getInventory());
                mc.getNetworkHandler().sendPacket(new PlaceRecipeC2SPacket_1_12(container.syncId, transactionId, transactionFromMatrix, new ArrayList<>()));

                if (iRecipeBookWidget.getRecipeBook().isFilteringCraftable(container)) {
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

        boolean alreadyPlaced = container.matches(recipe);
        int possibleCraftCount = iRecipeBookWidget.getRecipeFinder().countRecipeCrafts(recipe, null);

        if (alreadyPlaced) {
            // check each item in the input to see if we're already at the max crafts possible
            boolean canPlaceMore = false;

            for (int i = 0; i < container.getCraftingSlotCount(); i++) {
                if (i == container.getCraftingResultSlotIndex())
                    continue;

                ItemStack stack = container.getSlot(i).getStack();

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
        if (iRecipeBookWidget.getRecipeFinder().findRecipe(recipe, inputItemIds, craftCount)) {
            // take into account max stack sizes now we've found the actual inputs
            int actualCount = craftCount;

            for (int itemId : inputItemIds) {
                int maxCount = RecipeFinder.getStackFromId(itemId).getMaxCount();

                if (actualCount > maxCount) {
                    actualCount = maxCount;
                }
            }

            if (iRecipeBookWidget.getRecipeFinder().findRecipe(recipe, inputItemIds, actualCount)) {
                // clear the craft matrix and place the recipe
                List<PlaceRecipeC2SPacket_1_12.Transaction> transactionsFromMatrix = clearCraftMatrix();
                List<PlaceRecipeC2SPacket_1_12.Transaction> transactionsToMatrix = new ArrayList<>();
                placeRecipe(recipe, slots, actualCount, inputItemIds, transactionsToMatrix);
                short transactionId = mc.player.currentScreenHandler.getNextActionId(mc.player.getInventory());
                mc.getNetworkHandler().sendPacket(new PlaceRecipeC2SPacket_1_12(container.syncId, transactionId, transactionsFromMatrix, transactionsToMatrix));
                mc.player.getInventory().markDirty();
            }
        }
    }

    private List<PlaceRecipeC2SPacket_1_12.Transaction> clearCraftMatrix() {
        assert mc.player != null;

        iRecipeBookWidget.getGhostSlots().reset();
        PlayerInventory playerInv = mc.player.getInventory();
        List<PlaceRecipeC2SPacket_1_12.Transaction> transactionsFromMatrix = new ArrayList<>();

        int serverSlot = 1;
        for (int i = 0; i < container.getCraftingSlotCount(); i++) {
            if (i == container.getCraftingResultSlotIndex()) continue;

            ItemStack stack = container.getSlot(i).getStack();

            if (!stack.isEmpty()) {
                while (stack.getCount() > 0) {
                    int destSlot = getOccupiedSlotWithRoomForStack(playerInv, stack);

                    if (destSlot == -1) {
                        destSlot = playerInv.getEmptySlot();
                    }

                    ItemStack originalStack = stack.copy();
                    ItemStack targetStack = stack.copy();
                    targetStack.setCount(1);

                    if (playerInv.insertStack(destSlot, targetStack)) {
                        targetStack.increment(1);
                    } else {
                        // This shouldn't happen - condition already checked by canClearCraftMatrix
                        LogManager.getLogger().error("Can't find any space for item in inventory");
                    }

                    container.getSlot(i).takeStack(1);
                    transactionsFromMatrix.add(new PlaceRecipeC2SPacket_1_12.Transaction(originalStack, targetStack.copy(), serverSlot, destSlot));
                }
            }

            serverSlot++;
        }

        container.clearCraftingSlots();

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
            for (int i = 0; i < container.getCraftingSlotCount(); i++) {
                if (i == container.getCraftingResultSlotIndex()) continue;

                ItemStack stack = container.getSlot(i).getStack();
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

    private void placeRecipe(Recipe<C> recipe, List<Slot> slots, int placeCount, IntList inputItemIds, List<PlaceRecipeC2SPacket_1_12.Transaction> transactionsToMatrix) {
        int width = container.getCraftingWidth();
        int height = container.getCraftingHeight();

        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shaped = (ShapedRecipe) recipe;
            width = shaped.getWidth();
            height = shaped.getHeight();
        }

        int serverSlot = 1;
        Iterator<Integer> inputItemItr = inputItemIds.iterator();

        // :thonkjang: probably meant to swap craftingWidth and craftingHeight here, but oh well because width = height
        for (int y = 0; y < container.getCraftingWidth() && y != height; y++) {
            for (int x = 0; x < container.getCraftingHeight(); x++) {
                if (x == width || !inputItemItr.hasNext()) {
                    serverSlot += container.getCraftingWidth() - x;
                    break;
                }

                Slot slot = slots.get(serverSlot);

                ItemStack stackNeeded = RecipeFinder.getStackFromId(inputItemItr.next());
                if (!stackNeeded.isEmpty()) {
                    for (int i = 0; i < placeCount; i++) {
                        PlaceRecipeC2SPacket_1_12.Transaction transaction = findAndMoveToCraftMatrix(serverSlot, slot, stackNeeded);
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

    private PlaceRecipeC2SPacket_1_12.Transaction findAndMoveToCraftMatrix(int destSlotIndex, Slot destSlot, ItemStack stackNeeded) {
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

                if (destSlot.getStack().isEmpty()) {
                    destSlot.setStack(stack);
                } else {
                    destSlot.getStack().increment(1);
                }

                return new PlaceRecipeC2SPacket_1_12.Transaction(originalStack, stack, destSlotIndex, fromSlot);
            }
        }
    }

    private boolean canClearCraftMatrix() {
        assert mc.player != null;

        PlayerInventory invPlayer = mc.player.getInventory();

        for (int i = 0; i < container.getCraftingSlotCount(); ++i) {
            if (i == container.getCraftingResultSlotIndex()) continue;

            ItemStack stack = container.getSlot(i).getStack();

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
                && ItemStack.areTagsEqual(existingStack, stack)
                && existingStack.isStackable()
                && existingStack.getCount() < existingStack.getMaxCount()
                && existingStack.getCount() < 64;
    }

}
