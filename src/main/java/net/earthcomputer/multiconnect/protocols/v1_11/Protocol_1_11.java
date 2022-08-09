package net.earthcomputer.multiconnect.protocols.v1_11;

import net.earthcomputer.multiconnect.protocols.v1_12.RecipeInfo;
import net.minecraft.world.item.Items;
import java.util.List;

public class Protocol_1_11 extends Protocol_1_11_2 {

    @Override
    public List<RecipeInfo<?>> getRecipes() {
        List<RecipeInfo<?>> recipes = super.getRecipes();
        recipes.removeIf(recipe -> recipe.getOutput().getItem() == Items.IRON_NUGGET);
        recipes.removeIf(recipe -> recipe.getDistinguisher().equals("iron_nugget_to_ingot"));
        return recipes;
    }
}
