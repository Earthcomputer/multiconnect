package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketUpdateRecipes;
import net.earthcomputer.multiconnect.packets.v1_13_2.ItemStack_1_13_2;
import net.earthcomputer.multiconnect.packets.v1_13_2.SPacketUpdateRecipes_1_13_2;
import net.earthcomputer.multiconnect.packets.v1_15_2.ItemStack_1_15_2;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@MessageVariant(minVersion = Protocols.V1_14)
public class SPacketUpdateRecipes_Latest implements SPacketUpdateRecipes {
    @Introduce(compute = "computeRecipes")
    public List<Recipe> recipes;

    public static List<Recipe> computeRecipes(
            @Argument("recipes") List<SPacketUpdateRecipes_1_13_2.RecipeWithId> recipes,
            @FilledArgument(fromVersion = Protocols.V1_13_2, toVersion = Protocols.V1_14) Function<ItemStack_1_13_2, ItemStack_1_15_2> itemStackTranslator,
            @FilledArgument(fromVersion = Protocols.V1_13_2, toVersion = Protocols.V1_14) Function<SPacketUpdateRecipes_Latest.Ingredient, SPacketUpdateRecipes_Latest.Ingredient> ingredientTranslator
    ) {
        return recipes.stream()
                .map(recipe -> {
                    Recipe newRecipe;
                    if (recipe.recipe instanceof SPacketUpdateRecipes_1_13_2.CraftingShapeless shapeless) {
                        var newShapeless = new CraftingShapeless();
                        newShapeless.group = shapeless.group;
                        newShapeless.ingredients = shapeless.ingredients.stream().map(ingredientTranslator).collect(Collectors.toCollection(ArrayList::new));
                        newShapeless.result = itemStackTranslator.apply((ItemStack_1_13_2) shapeless.result);
                        newRecipe = newShapeless;
                    } else if (recipe.recipe instanceof SPacketUpdateRecipes_1_13_2.CraftingShaped shaped) {
                        var newShaped = new CraftingShaped();
                        newShaped.width = shaped.width;
                        newShaped.height = shaped.height;
                        newShaped.group = shaped.group;
                        newShaped.ingredients = shaped.ingredients.stream().map(ingredientTranslator).collect(Collectors.toCollection(ArrayList::new));
                        newShaped.result = itemStackTranslator.apply((ItemStack_1_13_2) shaped.result);
                        newRecipe = newShaped;
                    } else if (recipe.recipe instanceof SPacketUpdateRecipes_1_13_2.Smelting smelting) {
                        var newSmelting = new Smelting();
                        newSmelting.group = smelting.group;
                        newSmelting.ingredient = ingredientTranslator.apply(smelting.ingredient);
                        newSmelting.result = itemStackTranslator.apply((ItemStack_1_13_2) smelting.result);
                        newSmelting.experience = smelting.experience;
                        newSmelting.cookingTime = smelting.cookingTime;
                        newRecipe = newSmelting;
                    } else {
                        newRecipe = new Special();
                    }
                    newRecipe.recipeId = recipe.recipeId;
                    newRecipe.type = new ResourceLocation(recipe.recipe.type);
                    return newRecipe;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Polymorphic
    @MessageVariant
    public static abstract class Recipe {
        @Registry(Registries.RECIPE_SERIALIZER)
        public ResourceLocation type;
        public ResourceLocation recipeId;
    }

    @Polymorphic(stringValue = "crafting_shapeless")
    @MessageVariant
    public static class CraftingShapeless extends Recipe {
        public String group;
        public List<Ingredient> ingredients;
        public CommonTypes.ItemStack result;
    }

    @Polymorphic(stringValue = "crafting_shaped")
    @MessageVariant
    public static class CraftingShaped extends Recipe {
        public int width;
        public int height;
        public String group;
        @Length(compute = "computeLength")
        public List<Ingredient> ingredients;
        public CommonTypes.ItemStack result;

        public static int computeLength(@Argument("width") int width, @Argument("height") int height) {
            return width * height;
        }
    }

    @Polymorphic(stringValue = {"smelting", "blasting", "smoking", "campfire_cooking"})
    @MessageVariant
    public static class Smelting extends Recipe {
        public String group;
        public Ingredient ingredient;
        public CommonTypes.ItemStack result;
        public float experience;
        public int cookingTime;
    }

    @Polymorphic(stringValue = "stonecutting")
    @MessageVariant
    public static class Stonecutting extends Recipe {
        public String group;
        public Ingredient ingredient;
        public CommonTypes.ItemStack result;
    }

    @Polymorphic(stringValue = "smithing")
    @MessageVariant
    public static class Smithing extends Recipe {
        public Ingredient base;
        public Ingredient addition;
        public CommonTypes.ItemStack result;
    }

    @Polymorphic(otherwise = true)
    @MessageVariant
    public static class Special extends Recipe {
    }

    @MessageVariant
    public static class Ingredient {
        public List<CommonTypes.ItemStack> options;
    }
}
