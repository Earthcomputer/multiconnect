package net.earthcomputer.multiconnect.protocols.v1_11.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_11.RecipeBookEmulator;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin extends RecipeBookMenu<CraftingContainer> {

    @Shadow @Final private CraftingContainer craftSlots;

    public InventoryMenuMixin(MenuType<?> screenHandlerType, int syncId) {
        super(screenHandlerType, syncId);
    }

    @Inject(method = "slotsChanged", at = @At("HEAD"))
    private void onUpdateResult(CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_11_2) {
            RecipeBookEmulator.setCraftingResultSlot(containerId, this, craftSlots);
        }
    }

}
