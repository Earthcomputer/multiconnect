package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.minecraft.util.Identifier;

import java.util.List;

@MessageVariant
public class SPacketSynchronizeRecipes {
    public List<Recipe> recipes;

    @Polymorphic
    @MessageVariant
    public static abstract class Recipe {
        @Registry(Registries.RECIPE_SERIALIZER)
        public Identifier type;
        public Identifier recipeId;
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
