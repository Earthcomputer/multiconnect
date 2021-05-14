package net.earthcomputer.multiconnect.protocols.v1_12;

import net.minecraft.client.gui.screen.recipebook.RecipeBookGhostSlots;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.recipe.RecipeMatcher;

public interface IRecipeBookWidget {

    RecipeBookGhostSlots getGhostSlots();

    boolean multiconnect_isWide();

    ClientRecipeBook getRecipeBook();

    RecipeMatcher getRecipeFinder();

}
