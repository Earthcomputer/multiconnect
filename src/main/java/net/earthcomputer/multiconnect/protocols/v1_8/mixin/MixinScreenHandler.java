package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ScreenHandler.class)
public class MixinScreenHandler {
    @Shadow @Final public List<Slot> slots;

    @Inject(method = "method_30010", at = @At("HEAD"), cancellable = true)
    private void onSetSlot(int slot, int clickData, SlotActionType actionType, PlayerEntity player, CallbackInfoReturnable<ItemStack> ci) {
        if (actionType == SlotActionType.SWAP && clickData == 40) {
            ci.setReturnValue(this.slots.get(slot).getStack());
        }
    }
}
