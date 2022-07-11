package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.inventory.BrewingStandMenu$FuelSlot")
public class BrewingStandFuelSlotMixin extends Slot {
    public BrewingStandFuelSlotMixin(Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Inject(method = "mayPlaceItem", at = @At("HEAD"), cancellable = true)
    private static void removeFuelSlot(CallbackInfoReturnable<Boolean> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            ci.setReturnValue(false);
        }
    }

    @Override
    public boolean isActive() {
        return ConnectionInfo.protocolVersion > Protocols.V1_8;
    }
}
