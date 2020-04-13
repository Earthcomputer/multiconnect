package net.earthcomputer.multiconnect.protocols.v1_12;

import net.minecraft.client.gui.recipebook.GhostRecipe;
import net.minecraft.client.util.ClientRecipeBook;
import net.minecraft.item.crafting.RecipeItemHelper;

public interface IRecipeBookWidget {

    GhostRecipe getGhostRecipe();

    boolean multiconnect_isWide();

    ClientRecipeBook getRecipeBook();

    RecipeItemHelper getStackedContents();

}
