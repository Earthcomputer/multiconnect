package net.earthcomputer.multiconnect.protocols.v1_12.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_12.IRecipeBookWidget;
import net.earthcomputer.multiconnect.protocols.v1_12.RecipeBook_1_12;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.recipebook.GhostRecipe;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.recipebook.RecipeBookPage;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.util.ClientRecipeBook;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.RecipeBookContainer;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeItemHelper;
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

@Mixin(RecipeBookGui.class)
public abstract class MixinRecipeBookWidget implements IRecipeBookWidget {

    @Shadow @Final protected RecipeBookPage recipeBookPage;

    @Shadow protected abstract boolean isOffsetNextToMainGUI();

    @Shadow protected abstract void setVisible(boolean opened);

    @Unique private RecipeBook_1_12<?> recipeBook112;

    @Inject(method = "init", at = @At("RETURN"))
    private void onInitialize(int parentWidth, int parentHeight, Minecraft mc, boolean isNarrow, RecipeBookContainer<?> craftingContainer, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12) {
            recipeBook112 = new RecipeBook_1_12((RecipeBookGui) (Object) this, this, craftingContainer);
        }
    }

    @Inject(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/recipebook/RecipeList;isCraftable(Lnet/minecraft/item/crafting/IRecipe;)Z"), cancellable = true)
    private void redirectRecipeBook(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12) {
            handleRecipeClicked(recipeBook112, recipeBookPage.getLastClickedRecipe(), recipeBookPage.getLastClickedRecipeList());
            if (!isOffsetNextToMainGUI())
                setVisible(false);
            ci.setReturnValue(true);
        }
    }

    @SuppressWarnings("unchecked")
    @Unique
    private static <C extends Inventory> void handleRecipeClicked(RecipeBook_1_12<C> recipeBook112, IRecipe<?> recipe, RecipeList results) {
        recipeBook112.handleRecipeClicked((IRecipe<C>) recipe, results);
    }

    @Invoker("isOffsetNextToMainGUI")
    @Override
    public abstract boolean multiconnect_isWide();

    @Accessor
    @Override
    public abstract GhostRecipe getGhostRecipe();

    @Accessor
    @Override
    public abstract ClientRecipeBook getRecipeBook();

    @Accessor
    @Override
    public abstract RecipeItemHelper getStackedContents();
}
