package net.earthcomputer.multiconnect.protocols.v1_12;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RecipeBook_1_12<C extends Container> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Minecraft mc = Minecraft.getInstance();
    private final RecipeBookComponent recipeBookComponent;
    private final IRecipeBookComponent iRecipeBookComponent;
    private final RecipeBookMenu<C> menu;

    public RecipeBook_1_12(RecipeBookComponent recipeBookComponent, IRecipeBookComponent iRecipeBookComponent, RecipeBookMenu<C> menu) {
        this.recipeBookComponent = recipeBookComponent;
        this.iRecipeBookComponent = iRecipeBookComponent;
        this.menu = menu;
    }

    public void handleRecipeClicked(Recipe<C> recipe, RecipeCollection recipes) {
        assert mc.player != null;
        assert mc.getConnection() != null;

        boolean craftable = recipes.isCraftable(recipe);

        if (!craftable && iRecipeBookComponent.getGhostRecipe().getRecipe() == recipe) {
            return;
        }

        if (!canClearCraftMatrix() && !mc.player.isCreative()) {
            return;
        }

        if (craftable) {
            tryPlaceRecipe(recipe, menu.slots);
        } else {
            // clear craft matrix and show ghost recipe
            var transactionsFromMatrix = clearCraftMatrix();
            recipeBookComponent.setupGhostRecipe(recipe, menu.slots);

            if (!transactionsFromMatrix.isEmpty()) {
                // TODO: rewrite for via
//                var packet = new CPacketPlaceRecipe_1_12();
//                packet.syncId = menu.containerId;
//                packet.transactionId = Protocol_1_16_5.nextScreenActionId();
//                packet.transactionsToMatrix = new ArrayList<>();
//                packet.transactionsFromMatrix = transactionsFromMatrix.stream().map(Transaction::toPacketTransaction).collect(Collectors.toCollection(ArrayList::new));
//                PacketSystem.sendToServer(mc.getConnection(), Protocols.V1_12, packet);

                if (iRecipeBookComponent.getBook().isFiltering(menu)) {
                    mc.player.getInventory().setChanged();
                }
            }
        }

        if (!iRecipeBookComponent.multiconnect_isOffsetNextToMainGUI()) {
            recipeBookComponent.toggleVisibility();
        }
    }

    private void tryPlaceRecipe(Recipe<C> recipe, List<Slot> slots) {
        assert mc.player != null;
        assert mc.getConnection() != null;

        boolean alreadyPlaced = menu.recipeMatches(recipe);
        int possibleCraftCount = iRecipeBookComponent.getStackedContents().getBiggestCraftableStack(recipe, null);

        if (alreadyPlaced) {
            // check each item in the input to see if we're already at the max crafts possible
            boolean canPlaceMore = false;

            for (int i = 0; i < menu.getSize(); i++) {
                if (!isPartOfCraftMatrix(i))
                    continue;

                ItemStack stack = menu.getSlot(i).getItem();

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
        if (iRecipeBookComponent.getStackedContents().canCraft(recipe, inputItemIds, craftCount)) {
            // take into account max stack sizes now we've found the actual inputs
            int actualCount = craftCount;

            for (int itemId : inputItemIds) {
                int maxCount = StackedContents.fromStackingIndex(itemId).getMaxStackSize();

                if (actualCount > maxCount) {
                    actualCount = maxCount;
                }
            }

            if (iRecipeBookComponent.getStackedContents().canCraft(recipe, inputItemIds, actualCount)) {
                // clear the craft matrix and place the recipe
                var transactionsFromMatrix = clearCraftMatrix();
                var transactionsToMatrix = new ArrayList<Transaction>();
                placeRecipe(recipe, slots, actualCount, inputItemIds, transactionsToMatrix);
                // TODO: rewrite for via
//                var packet = new CPacketPlaceRecipe_1_12();
//                packet.syncId = menu.containerId;
//                packet.transactionId = Protocol_1_16_5.nextScreenActionId();
//                packet.transactionsToMatrix = transactionsToMatrix.stream().map(Transaction::toPacketTransaction).collect(Collectors.toCollection(ArrayList::new));
//                packet.transactionsFromMatrix = transactionsFromMatrix.stream().map(Transaction::toPacketTransaction).collect(Collectors.toCollection(ArrayList::new));
//                PacketSystem.sendToServer(mc.getConnection(), Protocols.V1_12, packet);
            }
        }
    }

    private List<Transaction> clearCraftMatrix() {
        assert mc.player != null;

        iRecipeBookComponent.getGhostRecipe().clear();
        Inventory playerInv = mc.player.getInventory();
        var transactionsFromMatrix = new ArrayList<Transaction>();

        int serverSlot = 1;
        for (int i = 0; i < menu.getSize(); i++) {
            if (!isPartOfCraftMatrix(i)) continue;

            ItemStack stack = menu.getSlot(i).getItem();

            if (!stack.isEmpty()) {
                while (stack.getCount() > 0) {
                    int destSlot = getOccupiedSlotWithRoomForStack(playerInv, stack);

                    if (destSlot == -1) {
                        destSlot = playerInv.getFreeSlot();
                    }

                    if (destSlot == -1) {
                        // Wtf, this happens?!
                        break;
                    }

                    ItemStack originalStack = stack.copy();
                    ItemStack targetStack = stack.copy();
                    targetStack.setCount(1);
                    ItemStack placedOn = playerInv.getItem(destSlot).copy();

                    if (playerInv.add(destSlot, targetStack)) {
                        targetStack.grow(1);
                    } else {
                        // This shouldn't happen - condition already checked by canClearCraftMatrix
                        LOGGER.error("Can't find any space for item in inventory");
                    }

                    menu.getSlot(i).remove(1);
                    transactionsFromMatrix.add(new Transaction(originalStack,
                            targetStack.copy(), placedOn, serverSlot, destSlot));
                }
            }

            serverSlot++;
        }

        menu.clearCraftingContent();

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
            for (int i = 0; i < menu.getSize(); i++) {
                if (!isPartOfCraftMatrix(i)) continue;

                ItemStack stack = menu.getSlot(i).getItem();
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
        int width = menu.getGridWidth();
        int height = menu.getGridHeight();

        if (recipe instanceof ShapedRecipe shaped) {
            width = shaped.getWidth();
            height = shaped.getHeight();
        }

        int serverSlot;
        for (serverSlot = 0; !isPartOfCraftMatrix(serverSlot); serverSlot++)
            ;

        Iterator<Integer> inputItemItr = inputItemIds.iterator();

        // :thonkjang: probably meant to swap craftingWidth and craftingHeight here, but oh well because width = height
        for (int y = 0; y < menu.getGridWidth() && y != height; y++) {
            for (int x = 0; x < menu.getGridHeight(); x++) {
                if (x == width || !inputItemItr.hasNext()) {
                    serverSlot += menu.getGridWidth() - x;
                    break;
                }

                Slot slot = slots.get(serverSlot);

                ItemStack stackNeeded = StackedContents.fromStackingIndex(inputItemItr.next());
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

        Inventory playerInv = mc.player.getInventory();
        int fromSlot = playerInv.findSlotMatchingUnusedItem(stackNeeded);

        if (fromSlot == -1) {
            return null;
        } else {
            ItemStack stack = playerInv.getItem(fromSlot).copy();

            if (stack.isEmpty()) {
                LOGGER.error("Matched: {} with empty item.", stackNeeded.getDescriptionId());
                return null;
            } else {
                if (stack.getCount() > 1) {
                    playerInv.removeItem(fromSlot, 1);
                } else {
                    playerInv.removeItemNoUpdate(fromSlot);
                }

                ItemStack originalStack = stack.copy();
                stack.setCount(1);
                ItemStack placedOn = destSlot.getItem().copy();

                if (destSlot.getItem().isEmpty()) {
                    destSlot.set(stack);
                } else {
                    destSlot.getItem().grow(1);
                }

                return new Transaction(originalStack, stack, placedOn, destSlotIndex, fromSlot);
            }
        }
    }

    private boolean canClearCraftMatrix() {
        assert mc.player != null;

        Inventory invPlayer = mc.player.getInventory();

        for (int i = 0; i < menu.getSize(); ++i) {
            if (!isPartOfCraftMatrix(i)) continue;

            ItemStack stack = menu.getSlot(i).getItem();

            if (!stack.isEmpty()) {
                int destStack = getOccupiedSlotWithRoomForStack(invPlayer, stack);

                if (destStack == -1) {
                    destStack = invPlayer.getFreeSlot();
                }

                if (destStack == -1) {
                    return false;
                }
            }
        }

        return true;
    }

    private int getOccupiedSlotWithRoomForStack(Inventory playerInv, ItemStack stack) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_11_2) {
            if (canStackAddMore(playerInv.getItem(playerInv.selected), stack)) {
                return playerInv.selected;
            }
            for (int j = 0; j < playerInv.items.size(); j++) {
                if (canStackAddMore(playerInv.items.get(j), stack)) {
                    return j;
                }
            }
            return -1;
        } else {
            return playerInv.getSlotWithRemainingSpace(stack);
        }
    }

    private boolean canStackAddMore(ItemStack existingStack, ItemStack stack) {
        return !existingStack.isEmpty()
                && existingStack.getItem() == stack.getItem()
                && ItemStack.tagMatches(existingStack, stack)
                && existingStack.isStackable()
                && existingStack.getCount() < existingStack.getMaxStackSize()
                && existingStack.getCount() < 64;
    }

    private boolean isPartOfCraftMatrix(int slotId) {
        if (menu instanceof AbstractFurnaceMenu && slotId == 1) {
            // exclude fuel slot
            return false;
        }
        return slotId < menu.getSize() && slotId != menu.getResultSlotIndex();
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

        // TODO: rewrite for via
//        private CPacketPlaceRecipe_1_12.Transaction toPacketTransaction() {
//            var result = new CPacketPlaceRecipe_1_12.Transaction();
//            result.stack = ItemStack_1_12_2.fromMinecraft(this.stack);
//            result.craftingSlot = (byte) this.craftingSlot;
//            result.invSlot = (byte) this.invSlot;
//            return result;
//        }
    }
}
