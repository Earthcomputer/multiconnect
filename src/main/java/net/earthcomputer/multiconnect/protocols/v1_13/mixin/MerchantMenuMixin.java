package net.earthcomputer.multiconnect.protocols.v1_13.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// TODO: is this applicable for via?
@Mixin(MerchantMenu.class)
public abstract class MerchantMenuMixin extends AbstractContainerMenu {

    @Shadow @Final private MerchantContainer tradeContainer;

    @Shadow public abstract MerchantOffers getOffers();

    protected MerchantMenuMixin(MenuType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "tryMoveItems", at = @At("HEAD"), cancellable = true)
    private void onTryMoveItems(int recipeId, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion > Protocols.V1_13_2)
            return;
        ci.cancel();

        if (recipeId >= getOffers().size())
            return;

        var interactionManager = Minecraft.getInstance().gameMode;
        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        assert interactionManager != null;

        // move 1st input slot to inventory
        if (!tradeContainer.getItem(0).isEmpty()) {
            int count = tradeContainer.getItem(0).getCount();
            interactionManager.handleInventoryMouseClick(containerId, 0, 0, ClickType.QUICK_MOVE, player);
            if (count == tradeContainer.getItem(0).getCount())
                return;
        }

        // move 2nd input slot to inventory
        if (!tradeContainer.getItem(1).isEmpty()) {
            int count = tradeContainer.getItem(1).getCount();
            interactionManager.handleInventoryMouseClick(containerId, 1, 0, ClickType.QUICK_MOVE, player);
            if (count == tradeContainer.getItem(1).getCount())
                return;
        }

        // refill the slots
        if (tradeContainer.getItem(0).isEmpty() && tradeContainer.getItem(1).isEmpty()) {
            multiconnect_autofill(interactionManager, player, 0, getOffers().get(recipeId).getCostA());
            multiconnect_autofill(interactionManager, player, 1, getOffers().get(recipeId).getCostB());
        }
    }

    @Unique
    private void multiconnect_autofill(MultiPlayerGameMode interactionManager, LocalPlayer player,
                                       int inputSlot, ItemStack stackNeeded) {
        if (stackNeeded.isEmpty())
            return;

        int slot;
        for (slot = 3; slot < 39; slot++) {
            ItemStack stack = slots.get(slot).getItem();
            if (stack.getItem() == stackNeeded.getItem() && ItemStack.tagMatches(stack, stackNeeded)) {
                break;
            }
        }
        if (slot == 39)
            return;

        boolean wasHoldingItem = !player.containerMenu.getCarried().isEmpty();
        interactionManager.handleInventoryMouseClick(containerId, slot, 0, ClickType.PICKUP, player);
        interactionManager.handleInventoryMouseClick(containerId, slot, 0, ClickType.PICKUP_ALL, player);
        interactionManager.handleInventoryMouseClick(containerId, inputSlot, 0, ClickType.PICKUP, player);
        if (wasHoldingItem)
            interactionManager.handleInventoryMouseClick(containerId, slot, 0, ClickType.PICKUP, player);
    }

    @Inject(method = "canTakeItemForPickAll", at = @At("HEAD"), cancellable = true)
    private void modifyCanTakeItemForPickAll(ItemStack stack, Slot slot, CallbackInfoReturnable<Boolean> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2)
            ci.setReturnValue(true);
    }

}
