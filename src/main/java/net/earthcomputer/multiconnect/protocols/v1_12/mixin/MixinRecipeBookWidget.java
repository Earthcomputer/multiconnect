package net.earthcomputer.multiconnect.protocols.v1_12.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_12.IRecipeBookWidget;
import net.earthcomputer.multiconnect.protocols.v1_12.RecipeBook_1_12;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.recipebook.RecipeBookGhostSlots;
import net.minecraft.client.gui.screen.recipebook.RecipeBookResults;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.screen.AbstractRecipeScreenHandler;
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

@Mixin(RecipeBookWidget.class)
public abstract class MixinRecipeBookWidget implements IRecipeBookWidget {

    @Shadow @Final private RecipeBookResults recipesArea;

    @Shadow protected abstract boolean isWide();

    @Shadow protected abstract void setOpen(boolean opened);

    @Unique private RecipeBook_1_12<?> recipeBook112;

    @Inject(method = "initialize", at = @At("RETURN"))
    private void onInitialize(int parentWidth, int parentHeight, MinecraftClient mc, boolean isNarrow, AbstractRecipeScreenHandler<?> craftingContainer, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12) {
            recipeBook112 = new RecipeBook_1_12<>((RecipeBookWidget) (Object) this, this, craftingContainer);
        }
    }

    @Inject(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/recipebook/RecipeResultCollection;isCraftable(Lnet/minecraft/recipe/Recipe;)Z"), cancellable = true)
    private void redirectRecipeBook(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12) {
            handleRecipeClicked(recipeBook112, recipesArea.getLastClickedRecipe(), recipesArea.getLastClickedResults());
            if (!isWide())
                setOpen(false);
            ci.setReturnValue(true);
        }
    }

    @SuppressWarnings("unchecked")
    @Unique
    private static <C extends Inventory> void handleRecipeClicked(RecipeBook_1_12<C> recipeBook112, Recipe<?> recipe, RecipeResultCollection results) {
        recipeBook112.handleRecipeClicked((Recipe<C>) recipe, results);
    }

    @Invoker("isWide")
    @Override
    public abstract boolean multiconnect_isWide();

    @Accessor
    @Override
    public abstract RecipeBookGhostSlots getGhostSlots();

    @Accessor
    @Override
    public abstract ClientRecipeBook getRecipeBook();

    @Accessor
    @Override
    public abstract RecipeFinder getRecipeFinder();
}
