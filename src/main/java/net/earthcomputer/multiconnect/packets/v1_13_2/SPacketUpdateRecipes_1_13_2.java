package net.earthcomputer.multiconnect.packets.v1_13_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketUpdateRecipes;
import net.earthcomputer.multiconnect.packets.latest.SPacketUpdateRecipes_Latest;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

@MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_13_2)
public class SPacketUpdateRecipes_1_13_2 implements SPacketUpdateRecipes {
    public List<RecipeWithId> recipes;

    @MessageVariant
    public static class RecipeWithId {
        public ResourceLocation recipeId;
        public Recipe recipe;
    }

    @Polymorphic
    @MessageVariant
    public static abstract class Recipe {
        public String type;
    }

    @Polymorphic(stringValue = "crafting_shapeless")
    @MessageVariant
    public static class CraftingShapeless extends Recipe {
        public String group;
        public List<SPacketUpdateRecipes_Latest.Ingredient> ingredients;
        public CommonTypes.ItemStack result;
    }

    @Polymorphic(stringValue = "crafting_shaped")
    @MessageVariant
    public static class CraftingShaped extends Recipe {
        public int width;
        public int height;
        public String group;
        @Length(compute = "computeLength")
        public List<SPacketUpdateRecipes_Latest.Ingredient> ingredients;
        public CommonTypes.ItemStack result;

        public static int computeLength(@Argument("width") int width, @Argument("height") int height) {
            return width * height;
        }
    }

    @Polymorphic(stringValue = "smelting")
    @MessageVariant
    public static class Smelting extends Recipe {
        public String group;
        public SPacketUpdateRecipes_Latest.Ingredient ingredient;
        public CommonTypes.ItemStack result;
        public float experience;
        public int cookingTime;
    }

    @Polymorphic(otherwise = true)
    @MessageVariant
    public static class Special extends Recipe {
    }
}
