package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(PlayerScreenHandler.class)
public abstract class MixinPlayerScreenHandler extends AbstractRecipeScreenHandler<CraftingInventory> {
    public MixinPlayerScreenHandler(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Dynamic
    @Redirect(method = "<init>",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/screen/PlayerScreenHandler$2;<init>(Lnet/minecraft/screen/PlayerScreenHandler;Lnet/minecraft/inventory/Inventory;III)V")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/PlayerScreenHandler;addSlot(Lnet/minecraft/screen/slot/Slot;)Lnet/minecraft/screen/slot/Slot;", ordinal = 0))
    private Slot redirectAddOffhandSlot(PlayerScreenHandler screenHandler, Slot slot) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            return null;
        }
        return addSlot(slot);
    }

    @ModifyVariable(method = "transferSlot", ordinal = 0, at = @At(value = "STORE", ordinal = 0))
    private EquipmentSlot modifyEquipmentSlot(EquipmentSlot slot) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8 && slot == EquipmentSlot.OFFHAND) {
            return EquipmentSlot.MAINHAND;
        } else {
            return slot;
        }
    }
}
