package net.earthcomputer.multiconnect.protocols.v1_12.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_12.IRecipeBookComponent;
import net.earthcomputer.multiconnect.protocols.v1_12.RecipeBook_1_12;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.recipebook.GhostRecipe;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookComponentMixin implements IRecipeBookComponent {

    @Shadow @Final private RecipeBookPage recipeBookPage;
    @Shadow protected RecipeBookMenu<?> menu;

    @Shadow protected abstract boolean isOffsetNextToMainGUI();

    @Shadow protected abstract void setVisible(boolean visible);

    @Unique private RecipeBook_1_12<?> multiconnect_recipeBook112;

    @Inject(method = "init", at = @At("RETURN"))
    private void onInitialize(int parentWidth, int parentHeight, Minecraft mc, boolean isNarrow, RecipeBookMenu<?> menu, CallbackInfo ci) {
        if (multiconnect_shouldUse112RecipeBook()) {
            multiconnect_recipeBook112 = new RecipeBook_1_12<>((RecipeBookComponent) (Object) this, this, menu);
        }
    }

    @Inject(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeCollection;isCraftable(Lnet/minecraft/world/item/crafting/Recipe;)Z"), cancellable = true)
    private void redirectRecipeBook(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> ci) {
        if (multiconnect_shouldUse112RecipeBook()) {
            handleRecipeClicked(multiconnect_recipeBook112, recipeBookPage.getLastClickedRecipe(), recipeBookPage.getLastClickedRecipeCollection());
            if (!isOffsetNextToMainGUI())
                setVisible(false);
            ci.setReturnValue(true);
        }
    }

    @SuppressWarnings("unchecked")
    @Unique
    private static <C extends Container> void handleRecipeClicked(RecipeBook_1_12<C> recipeBook112, Recipe<?> recipe, RecipeCollection results) {
        recipeBook112.handleRecipeClicked((Recipe<C>) recipe, results);
    }

    @Invoker("isOffsetNextToMainGUI")
    @Override
    public abstract boolean multiconnect_isOffsetNextToMainGUI();

    @Accessor
    @Override
    public abstract GhostRecipe getGhostRecipe();

    @Accessor
    @Override
    public abstract ClientRecipeBook getBook();

    @Accessor
    @Override
    public abstract StackedContents getStackedContents();

    @Unique
    private boolean multiconnect_shouldUse112RecipeBook() {
        if (menu instanceof AbstractFurnaceMenu) {
            return ConnectionInfo.protocolVersion <= Protocols.V1_12_2;
        } else {
            return ConnectionInfo.protocolVersion <= Protocols.V1_12;
        }
    }
}
