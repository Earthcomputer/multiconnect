package net.earthcomputer.multiconnect.protocols.v1_11_2.mixin;

import net.earthcomputer.multiconnect.protocols.v1_11_2.IScreenHandler;
import net.earthcomputer.multiconnect.protocols.v1_11_2.RecipeBookEmulator;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ScreenHandler.class)
public class MixinScreenHandler implements IScreenHandler {

    @Shadow private short actionId;

    @Unique private RecipeBookEmulator recipeBookEmulator = new RecipeBookEmulator((ScreenHandler) (Object) this);

    @Override
    public short multiconnect_getCurrentActionId() {
        return actionId;
    }

    @Override
    public RecipeBookEmulator multiconnect_getRecipeBookEmulator() {
        return recipeBookEmulator;
    }
}
