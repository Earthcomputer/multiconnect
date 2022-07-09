package net.earthcomputer.multiconnect.protocols.v1_11.mixin;

import net.earthcomputer.multiconnect.protocols.v1_11.IContainerMenu;
import net.earthcomputer.multiconnect.protocols.v1_11.RecipeBookEmulator;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin implements IContainerMenu {

    @Unique private final RecipeBookEmulator multiconnect_recipeBookEmulator = new RecipeBookEmulator((AbstractContainerMenu) (Object) this);

    @Override
    public RecipeBookEmulator multiconnect_getRecipeBookEmulator() {
        return multiconnect_recipeBookEmulator;
    }

}
