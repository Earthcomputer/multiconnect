package net.earthcomputer.multiconnect.protocols.v1_11_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_11_2.IScreenHandler;
import net.earthcomputer.multiconnect.protocols.v1_11_2.RecipeBookEmulator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ScreenHandler.class)
public abstract class MixinScreenHandler implements IScreenHandler {

    @Shadow private short actionId;

    @Shadow public abstract ItemStack transferSlot(PlayerEntity player, int index);

    @Shadow @Final public List<Slot> slots;

    @Shadow protected abstract ItemStack method_30010(int i, int j, SlotActionType slotActionType, PlayerEntity playerEntity);

    @Unique private RecipeBookEmulator recipeBookEmulator = new RecipeBookEmulator((ScreenHandler) (Object) this);

    @Override
    public short multiconnect_getCurrentActionId() {
        return actionId;
    }

    @Override
    public RecipeBookEmulator multiconnect_getRecipeBookEmulator() {
        return recipeBookEmulator;
    }

    @Inject(method = "method_30010",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandler;transferSlot(Lnet/minecraft/entity/player/PlayerEntity;I)Lnet/minecraft/item/ItemStack;",
                    ordinal = 0,
                    shift = At.Shift.BEFORE), cancellable = true)
    public void quickMoveFix1_11_2(int slotId, int clickData, SlotActionType actionType, PlayerEntity playerEntity, CallbackInfoReturnable<ItemStack> cir) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_11_2) {
            ItemStack itemStack = ItemStack.EMPTY;
            final ItemStack itemstack6 = this.transferSlot(playerEntity, slotId);
            if (!itemstack6.isEmpty()) {
                itemStack = itemstack6.copy();
                if (this.slots.get(slotId).getStack().getItem() == itemstack6.getItem()) {
                    this.method_30010(slotId, clickData, SlotActionType.QUICK_MOVE, playerEntity);
                }
            }
            cir.setReturnValue(itemStack);
        }
    }

}
