package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin extends RecipeBookMenu<CraftingContainer> {
    public InventoryMenuMixin(MenuType<?> type, int syncId) {
        super(type, syncId);
    }

    @Redirect(method = "<init>",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/InventoryMenu$2;<init>(Lnet/minecraft/world/inventory/InventoryMenu;Lnet/minecraft/world/Container;III)V")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/InventoryMenu;addSlot(Lnet/minecraft/world/inventory/Slot;)Lnet/minecraft/world/inventory/Slot;", ordinal = 0))
    private Slot redirectAddOffhandSlot(InventoryMenu screenHandler, Slot slot) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            return null;
        }
        return addSlot(slot);
    }

    @ModifyVariable(method = "quickMoveStack", ordinal = 0, at = @At(value = "STORE", ordinal = 0))
    private EquipmentSlot modifyEquipmentSlot(EquipmentSlot slot) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8 && slot == EquipmentSlot.OFFHAND) {
            return EquipmentSlot.MAINHAND;
        } else {
            return slot;
        }
    }
}
