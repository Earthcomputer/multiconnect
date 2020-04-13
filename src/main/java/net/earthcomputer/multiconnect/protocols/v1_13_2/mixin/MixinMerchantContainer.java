package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.inventory.MerchantInventory;
import net.minecraft.inventory.container.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffers;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MerchantContainer.class)
public abstract class MixinMerchantContainer extends Container {

    @Shadow @Final private MerchantInventory merchantInventory;

    @Shadow public abstract MerchantOffers getOffers();

    protected MixinMerchantContainer(ContainerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "func_217046_g", at = @At("HEAD"), cancellable = true)
    private void onSwitchTo(int recipeId, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion > Protocols.V1_13_2)
            return;
        ci.cancel();

        if (recipeId >= getOffers().size())
            return;

        PlayerController interactionManager = Minecraft.getInstance().playerController;
        ClientPlayerEntity player = Minecraft.getInstance().player;
        assert player != null;
        assert interactionManager != null;

        // move 1st input slot to inventory
        if (!merchantInventory.getStackInSlot(0).isEmpty()) {
            int count = merchantInventory.getStackInSlot(0).getCount();
            interactionManager.windowClick(windowId, 0, 0, ClickType.QUICK_MOVE, player);
            if (count == merchantInventory.getStackInSlot(0).getCount())
                return;
        }

        // move 2nd input slot to inventory
        if (!merchantInventory.getStackInSlot(1).isEmpty()) {
            int count = merchantInventory.getStackInSlot(1).getCount();
            interactionManager.windowClick(windowId, 1, 0, ClickType.QUICK_MOVE, player);
            if (count == merchantInventory.getStackInSlot(1).getCount())
                return;
        }

        // refill the slots
        if (merchantInventory.getStackInSlot(0).isEmpty() && merchantInventory.getStackInSlot(1).isEmpty()) {
            autofill(interactionManager, player, 0, getOffers().get(recipeId).func_222205_b());
            autofill(interactionManager, player, 1, getOffers().get(recipeId).getBuyingStackSecond());
        }
    }

    @Unique
    private void autofill(PlayerController interactionManager, ClientPlayerEntity player,
                          int inputSlot, ItemStack stackNeeded) {
        if (stackNeeded.isEmpty())
            return;

        int slot;
        for (slot = 3; slot < 39; slot++) {
            ItemStack stack = inventorySlots.get(slot).getStack();
            if (stack.getItem() == stackNeeded.getItem() && ItemStack.areItemStackTagsEqual(stack, stackNeeded)) {
                break;
            }
        }
        if (slot == 39)
            return;

        boolean wasHoldingItem = !player.inventory.getItemStack().isEmpty();
        interactionManager.windowClick(windowId, slot, 0, ClickType.PICKUP, player);
        interactionManager.windowClick(windowId, slot, 0, ClickType.PICKUP_ALL, player);
        interactionManager.windowClick(windowId, inputSlot, 0, ClickType.PICKUP, player);
        if (wasHoldingItem)
            interactionManager.windowClick(windowId, slot, 0, ClickType.PICKUP, player);
    }

    @Inject(method = "canMergeSlot", at = @At("HEAD"), cancellable = true)
    private void modifyCanInsertIntoSlot(ItemStack stack, Slot slot, CallbackInfoReturnable<Boolean> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2)
            ci.setReturnValue(true);
    }

}
