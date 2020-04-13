package net.earthcomputer.multiconnect.protocols.v1_12;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.RecipeBookContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraftforge.common.crafting.IShapedRecipe;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RecipeBook_1_12<C extends Inventory> {

    private Minecraft mc = Minecraft.getInstance();
    private RecipeBookGui recipeBookWidget;
    private IRecipeBookWidget iRecipeBookWidget;
    private RecipeBookContainer<C> container;

    public RecipeBook_1_12(RecipeBookGui recipeBookWidget, IRecipeBookWidget iRecipeBookWidget, RecipeBookContainer<C> container) {
        this.recipeBookWidget = recipeBookWidget;
        this.iRecipeBookWidget = iRecipeBookWidget;
        this.container = container;
    }

    public void handleRecipeClicked(IRecipe<C> recipe, RecipeList recipes) {
        assert mc.player != null;
        assert mc.getConnection() != null;

        boolean craftable = recipes.isCraftable(recipe);

        if (!craftable && iRecipeBookWidget.getGhostRecipe().getRecipe() == recipe) {
            return;
        }

        if (!canClearCraftMatrix() && !mc.player.isCreative()) {
            return;
        }

        if (craftable) {
            tryPlaceRecipe(recipe, container.inventorySlots);
        } else {
            // clear craft matrix and show ghost recipe
            List<PlaceRecipeC2SPacket_1_12.Transaction> transactionFromMatrix = clearCraftMatrix();
            recipeBookWidget.setupGhostRecipe(recipe, container.inventorySlots);

            if (!transactionFromMatrix.isEmpty()) {
                short transactionId = mc.player.container.getNextTransactionID(mc.player.inventory);
                mc.getConnection().sendPacket(new PlaceRecipeC2SPacket_1_12(container.windowId, transactionId, transactionFromMatrix, new ArrayList<>()));

                if (iRecipeBookWidget.getRecipeBook().isFilteringCraftable()) {
                    mc.player.inventory.markDirty();
                }
            }
        }

        if (!iRecipeBookWidget.multiconnect_isWide()) {
            recipeBookWidget.toggleVisibility();
        }
    }

    private void tryPlaceRecipe(IRecipe<C> recipe, List<Slot> slots) {
        assert mc.player != null;
        assert mc.getConnection() != null;

        boolean alreadyPlaced = container.matches(recipe);
        int possibleCraftCount = iRecipeBookWidget.getStackedContents().getBiggestCraftableStack(recipe, null);

        if (alreadyPlaced) {
            // check each item in the input to see if we're already at the max crafts possible
            boolean canPlaceMore = false;

            for (int i = 0; i < container.getSize(); i++) {
                if (i == container.getOutputSlot())
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
        if (iRecipeBookWidget.getStackedContents().canCraft(recipe, inputItemIds, craftCount)) {
            // take into account max stack sizes now we've found the actual inputs
            int actualCount = craftCount;

            for (int itemId : inputItemIds) {
                int maxCount = RecipeItemHelper.unpack(itemId).getMaxStackSize();

                if (actualCount > maxCount) {
                    actualCount = maxCount;
                }
            }

            if (iRecipeBookWidget.getStackedContents().canCraft(recipe, inputItemIds, actualCount)) {
                // clear the craft matrix and place the recipe
                List<PlaceRecipeC2SPacket_1_12.Transaction> transactionsFromMatrix = clearCraftMatrix();
                List<PlaceRecipeC2SPacket_1_12.Transaction> transactionsToMatrix = new ArrayList<>();
                placeRecipe(recipe, slots, actualCount, inputItemIds, transactionsToMatrix);
                short transactionId = mc.player.container.getNextTransactionID(mc.player.inventory);
                mc.getConnection().sendPacket(new PlaceRecipeC2SPacket_1_12(container.windowId, transactionId, transactionsFromMatrix, transactionsToMatrix));
                mc.player.inventory.markDirty();
            }
        }
    }

    private List<PlaceRecipeC2SPacket_1_12.Transaction> clearCraftMatrix() {
        assert mc.player != null;

        iRecipeBookWidget.getGhostRecipe().clear();
        PlayerInventory playerInv = mc.player.inventory;
        List<PlaceRecipeC2SPacket_1_12.Transaction> transactionsFromMatrix = new ArrayList<>();

        int serverSlot = 1;
        for (int i = 0; i < container.getSize(); i++) {
            if (i == container.getOutputSlot()) continue;

            ItemStack stack = container.getSlot(i).getStack();

            if (!stack.isEmpty()) {
                while (stack.getCount() > 0) {
                    int destSlot = playerInv.getSlotFor(stack);

                    if (destSlot == -1) {
                        destSlot = playerInv.getFirstEmptyStack();
                    }

                    ItemStack targetStack = stack.copy();
                    targetStack.setCount(1);

                    if (playerInv.add(destSlot, targetStack)) {
                        targetStack.grow(1);
                    } else {
                        // This shouldn't happen - condition already checked by canClearCraftMatrix
                        LogManager.getLogger().error("Can't find any space for item in inventory");
                    }

                    container.getSlot(i).decrStackSize(1);
                    transactionsFromMatrix.add(new PlaceRecipeC2SPacket_1_12.Transaction(targetStack.copy(), serverSlot, destSlot));
                }
            }

            serverSlot++;
        }

        container.clear();

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
            for (int i = 0; i < container.getSize(); i++) {
                if (i == container.getOutputSlot()) continue;

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

    private void placeRecipe(IRecipe<C> recipe, List<Slot> slots, int placeCount, IntList inputItemIds, List<PlaceRecipeC2SPacket_1_12.Transaction> transactionsToMatrix) {
        int width = container.getWidth();
        int height = container.getHeight();

        if (recipe instanceof IShapedRecipe) {
            IShapedRecipe shaped = (IShapedRecipe) recipe;
            width = shaped.getRecipeWidth();
            height = shaped.getRecipeHeight();
        }

        int serverSlot = 1;
        Iterator<Integer> inputItemItr = inputItemIds.iterator();

        // :thonkjang: probably meant to swap craftingWidth and craftingHeight here, but oh well because width = height
        for (int y = 0; y < container.getWidth() && y != height; y++) {
            for (int x = 0; x < container.getHeight(); x++) {
                if (x == width || !inputItemItr.hasNext()) {
                    serverSlot += container.getWidth() - x;
                    break;
                }

                Slot slot = slots.get(serverSlot);

                ItemStack stackNeeded = RecipeItemHelper.unpack(inputItemItr.next());
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

        PlayerInventory playerInv = mc.player.inventory;
        int fromSlot = playerInv.getSlotFor(stackNeeded);

        if (fromSlot == -1) {
            return null;
        } else {
            ItemStack stack = playerInv.getStackInSlot(fromSlot).copy();

            if (stack.isEmpty()) {
                LogManager.getLogger().error("Matched: " + stackNeeded.getTranslationKey() + " with empty item.");
                return null;
            } else {
                if (stack.getCount() > 1) {
                    playerInv.decrStackSize(fromSlot, 1);
                } else {
                    playerInv.removeStackFromSlot(fromSlot);
                }

                stack.setCount(1);

                if (destSlot.getStack().isEmpty()) {
                    destSlot.putStack(stack);
                } else {
                    destSlot.getStack().grow(1);
                }

                return new PlaceRecipeC2SPacket_1_12.Transaction(stack, destSlotIndex, fromSlot);
            }
        }
    }

    private boolean canClearCraftMatrix() {
        assert mc.player != null;

        PlayerInventory invPlayer = mc.player.inventory;

        for (int i = 0; i < container.getSize(); ++i) {
            if (i == container.getOutputSlot()) continue;

            ItemStack stack = container.getSlot(i).getStack();

            if (!stack.isEmpty()) {
                int destStack = invPlayer.storeItemStack(stack);

                if (destStack == -1) {
                    destStack = invPlayer.getFirstEmptyStack();
                }

                if (destStack == -1) {
                    return false;
                }
            }
        }

        return true;
    }

}
