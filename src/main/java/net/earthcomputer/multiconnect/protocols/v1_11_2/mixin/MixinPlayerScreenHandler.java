package net.earthcomputer.multiconnect.protocols.v1_11_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_11_2.RecipeBookEmulator;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerScreenHandler.class)
public abstract class MixinPlayerScreenHandler extends AbstractRecipeScreenHandler<CraftingInventory> {

    @Shadow @Final private CraftingInventory craftingInput;

    public MixinPlayerScreenHandler(ScreenHandlerType<?> screenHandlerType, int syncId) {
        super(screenHandlerType, syncId);
    }

    @Inject(method = "onContentChanged", at = @At("HEAD"))
    private void onUpdateResult(CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_11_2) {
            RecipeBookEmulator.setCraftingResultSlot(syncId, this, craftingInput);
        }
    }

}
