package net.earthcomputer.multiconnect.protocols.v1_12;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.gui.screens.recipebook.GhostRecipe;
import net.minecraft.world.entity.player.StackedContents;

public interface IRecipeBookComponent {

    GhostRecipe getGhostRecipe();

    boolean multiconnect_isOffsetNextToMainGUI();

    ClientRecipeBook getBook();

    StackedContents getStackedContents();

}
