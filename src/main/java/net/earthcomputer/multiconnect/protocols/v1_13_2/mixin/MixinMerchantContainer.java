package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.village.MerchantInventory;
import net.minecraft.village.TradeOfferList;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MerchantScreenHandler.class)
public abstract class MixinMerchantContainer extends ScreenHandler {

    @Shadow @Final private MerchantInventory merchantInventory;

    @Shadow public abstract TradeOfferList getRecipes();

    protected MixinMerchantContainer(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "switchTo", at = @At("HEAD"), cancellable = true)
    private void onSwitchTo(int recipeId, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion > Protocols.V1_13_2)
            return;
        ci.cancel();

        if (recipeId >= getRecipes().size())
            return;

        var interactionManager = MinecraftClient.getInstance().interactionManager;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        assert player != null;
        assert interactionManager != null;

        // move 1st input slot to inventory
        if (!merchantInventory.getStack(0).isEmpty()) {
            int count = merchantInventory.getStack(0).getCount();
            interactionManager.clickSlot(syncId, 0, 0, SlotActionType.QUICK_MOVE, player);
            if (count == merchantInventory.getStack(0).getCount())
                return;
        }

        // move 2nd input slot to inventory
        if (!merchantInventory.getStack(1).isEmpty()) {
            int count = merchantInventory.getStack(1).getCount();
            interactionManager.clickSlot(syncId, 1, 0, SlotActionType.QUICK_MOVE, player);
            if (count == merchantInventory.getStack(1).getCount())
                return;
        }

        // refill the slots
        if (merchantInventory.getStack(0).isEmpty() && merchantInventory.getStack(1).isEmpty()) {
            autofill(interactionManager, player, 0, getRecipes().get(recipeId).getAdjustedFirstBuyItem());
            autofill(interactionManager, player, 1, getRecipes().get(recipeId).getSecondBuyItem());
        }
    }

    @Unique
    private void autofill(ClientPlayerInteractionManager interactionManager, ClientPlayerEntity player,
                          int inputSlot, ItemStack stackNeeded) {
        if (stackNeeded.isEmpty())
            return;

        int slot;
        for (slot = 3; slot < 39; slot++) {
            ItemStack stack = slots.get(slot).getStack();
            if (stack.getItem() == stackNeeded.getItem() && ItemStack.areTagsEqual(stack, stackNeeded)) {
                break;
            }
        }
        if (slot == 39)
            return;

        boolean wasHoldingItem = !player.currentScreenHandler.getCursorStack().isEmpty();
        interactionManager.clickSlot(syncId, slot, 0, SlotActionType.PICKUP, player);
        interactionManager.clickSlot(syncId, slot, 0, SlotActionType.PICKUP_ALL, player);
        interactionManager.clickSlot(syncId, inputSlot, 0, SlotActionType.PICKUP, player);
        if (wasHoldingItem)
            interactionManager.clickSlot(syncId, slot, 0, SlotActionType.PICKUP, player);
    }

    @Inject(method = "canInsertIntoSlot", at = @At("HEAD"), cancellable = true)
    private void modifyCanInsertIntoSlot(ItemStack stack, Slot slot, CallbackInfoReturnable<Boolean> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2)
            ci.setReturnValue(true);
    }

}
