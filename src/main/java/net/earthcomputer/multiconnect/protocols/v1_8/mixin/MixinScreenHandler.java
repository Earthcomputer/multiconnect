package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public class MixinScreenHandler {
    @Inject(method = "internalOnSlotClick", at = @At("HEAD"), cancellable = true)
    private void onSetSlot(int slot, int clickData, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (actionType == SlotActionType.SWAP && clickData == 40) {
            ci.cancel();
        }
    }
}
