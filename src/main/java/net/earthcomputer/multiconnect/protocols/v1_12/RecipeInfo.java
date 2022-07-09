package net.earthcomputer.multiconnect.protocols.v1_12;

import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.packets.latest.SPacketSynchronizeRecipes_Latest;
import net.earthcomputer.multiconnect.packets.v1_13_1.ItemStack_1_13_1;
import net.earthcomputer.multiconnect.packets.v1_13_2.SPacketSynchronizeRecipes_1_13_2;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.ItemLike;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class RecipeInfo<T extends Recipe<?>> {

    private final Function<ResourceLocation, Recipe<?>> creator;
    private final RecipeSerializer<T> recipeType;
    private final ItemStack output;
    private String distinguisher = "";

    private RecipeInfo(Function<ResourceLocation, Recipe<?>> creator, RecipeSerializer<T> recipeType, ItemStack output) {
        this.creator = creator;
        this.recipeType = recipeType;
        this.output = output;
    }

    public static <T extends Recipe<?>> RecipeInfo<T> of(Function<ResourceLocation, Recipe<?>> creator, RecipeSerializer<T> recipeType, ItemStack output) {
        return new RecipeInfo<>(creator, recipeType, output);
    }

    public static <T extends Recipe<?>> RecipeInfo<T> of(Function<ResourceLocation, Recipe<?>> creator, RecipeSerializer<T> recipeType, ItemLike output) {
        return of(creator, recipeType, new ItemStack(output));
    }

    public static <T extends Recipe<?>> RecipeInfo<T> of(Function<ResourceLocation, Recipe<?>> creator, RecipeSerializer<T> recipeType, ItemLike output, int count) {
        return of(creator, recipeType, new ItemStack(output, count));
    }

    public static RecipeInfo<ShapedRecipe> shaped(ItemStack output, Object... args) {
        return shaped("", output, args);
    }

    public static RecipeInfo<ShapedRecipe> shaped(ItemLike output, Object... args) {
        return shaped("", output, args);
    }

    public static RecipeInfo<ShapedRecipe> shaped(int count, ItemLike output, Object... args) {
        return shaped("", count, output, args);
    }

    public static RecipeInfo<ShapedRecipe> shaped(String group, ItemStack output, Object... args) {
        int i;
        int width = 0;
        List<String> shape = new ArrayList<>();
        for (i = 0; i < args.length && args[i] instanceof String str; i++) {
            if (i == 0)
                width = str.length();
            else if (str.length() != width)
                throw new IllegalArgumentException("Rows do not have consistent width");
            shape.add(str);
        }
        var legend = new HashMap<Character, Ingredient>();
        while (i < args.length && args[i] instanceof Character key) {
            i++;
            List<ItemLike> items = new ArrayList<>();
            for (; i < args.length && args[i] instanceof ItemLike; i++) {
                items.add((ItemLike) args[i]);
            }
            legend.put(key, Ingredient.of(items.toArray(new ItemLike[0])));
        }
        if (i != args.length)
            throw new IllegalArgumentException("Unexpected argument at index " + i + ": " + args[i]);

        int height = shape.size();
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (String row : shape) {
            for (int x = 0; x < width; x++) {
                char key = row.charAt(x);
                Ingredient ingredient = legend.get(key);
                if (ingredient == null) {
                    if (key == ' ')
                        ingredient = Ingredient.EMPTY;
                    else
                        throw new IllegalArgumentException("Unknown character in shape: " + key);
                }
                ingredients.add(ingredient);
            }
        }

        final int width_f = width;
        return new RecipeInfo<>(id -> new ShapedRecipe(id, group, width_f, height, ingredients, output),
                RecipeSerializer.SHAPED_RECIPE, output);
    }

    public static RecipeInfo<ShapedRecipe> shaped(String group, ItemLike output, Object... args) {
        return shaped(group, new ItemStack(output), args);
    }

    public static RecipeInfo<ShapedRecipe> shaped(String group, int count, ItemLike output, Object... args) {
        return shaped(group, new ItemStack(output, count), args);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(String group, ItemStack output, ItemLike... inputs) {
        ItemLike[][] newInputs = new ItemLike[inputs.length][1];
        for (int i = 0; i < inputs.length; i++)
            newInputs[i] = new ItemLike[] {inputs[i]};
        return shapeless(group, output, newInputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(String group, ItemLike output, ItemLike... inputs) {
        return shapeless(group, new ItemStack(output), inputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(String group, int count, ItemLike output, ItemLike... inputs) {
        return shapeless(group, new ItemStack(output, count), inputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(String group, ItemStack output, ItemLike[]... inputs) {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (ItemLike[] input : inputs) {
            ingredients.add(Ingredient.of(input));
        }
        return new RecipeInfo<>(id -> new ShapelessRecipe(id, group, output, ingredients), RecipeSerializer.SHAPELESS_RECIPE, output);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(String group, ItemLike output, ItemLike[]... inputs) {
        return shapeless(group, new ItemStack(output), inputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(String group, int count, ItemLike output, ItemLike[]... inputs) {
        return shapeless(group, new ItemStack(output, count), inputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(ItemStack output, ItemLike... inputs) {
        return shapeless("", output, inputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(ItemLike output, ItemLike... inputs) {
        return shapeless("", output, inputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(int count, ItemLike output, ItemLike... inputs) {
        return shapeless("", count, output, inputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(ItemStack output, ItemLike[]... inputs) {
        return shapeless("", output, inputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(ItemLike output, ItemLike[]... inputs) {
        return shapeless("", output, inputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(int count, ItemLike output, ItemLike[]... inputs) {
        return shapeless("", count, output, inputs);
    }

    public static RecipeInfo<SmeltingRecipe> smelting(ItemLike output, ItemLike input, float experience) {
        return smelting(output, input, experience, 200);
    }

    public static RecipeInfo<SmeltingRecipe> smelting(ItemLike output, Ingredient input, float experience) {
        return smelting(output, input, experience, 200);
    }

    public static RecipeInfo<SmeltingRecipe> smelting(ItemLike output, ItemLike input, float experience, int cookTime) {
        return smelting(output, Ingredient.of(input), experience, cookTime);
    }

    public static RecipeInfo<SmeltingRecipe> smelting(ItemLike output, Ingredient input, float experience, int cookTime) {
        ItemStack outputStack = new ItemStack(output);
        return new RecipeInfo<>(id -> new SmeltingRecipe(id, "", input, outputStack, experience, cookTime), RecipeSerializer.SMELTING_RECIPE, outputStack);
    }

    public RecipeInfo<T> distinguisher(String distinguisher) {
        this.distinguisher = distinguisher;
        return this;
    }


    public Recipe<?> create(ResourceLocation id) {
        return creator.apply(id);
    }

    public RecipeSerializer<T> getRecipeType() {
        return recipeType;
    }

    public ItemStack getOutput() {
        return output;
    }

    public String getDistinguisher() {
        return distinguisher;
    }

    public SPacketSynchronizeRecipes_1_13_2.RecipeWithId toPacketRecipe(ResourceLocation id) {
        var recipe = create(id);
        SPacketSynchronizeRecipes_1_13_2.Recipe packetRecipe;
        if (recipeType == RecipeSerializer.SMELTING_RECIPE) {
            var smelting = (SmeltingRecipe) recipe;
            var packetSmelting = new SPacketSynchronizeRecipes_1_13_2.Smelting();
            packetSmelting.group = smelting.getGroup();
            packetSmelting.ingredient = convertIngredient(smelting.getIngredients().get(0));
            packetSmelting.result = ItemStack_1_13_1.fromMinecraft(smelting.getResultItem());
            packetSmelting.experience = smelting.getExperience();
            packetSmelting.cookingTime = smelting.getCookingTime();
            packetRecipe = packetSmelting;
        } else if (recipeType == RecipeSerializer.SHAPED_RECIPE) {
            var shaped = (ShapedRecipe) recipe;
            var packetShaped = new SPacketSynchronizeRecipes_1_13_2.CraftingShaped();
            packetShaped.width = shaped.getWidth();
            packetShaped.height = shaped.getHeight();
            packetShaped.group = shaped.getGroup();
            packetShaped.ingredients = shaped.getIngredients().stream().map(RecipeInfo::convertIngredient).collect(Collectors.toCollection(ArrayList::new));
            packetShaped.result = ItemStack_1_13_1.fromMinecraft(shaped.getResultItem());
            packetRecipe = packetShaped;
        } else if (recipeType == RecipeSerializer.SHAPELESS_RECIPE) {
            var shapeless = (ShapelessRecipe) recipe;
            var packetShapeless = new SPacketSynchronizeRecipes_1_13_2.CraftingShapeless();
            packetShapeless.group = shapeless.getGroup();
            packetShapeless.ingredients = shapeless.getIngredients().stream().map(RecipeInfo::convertIngredient).collect(Collectors.toCollection(ArrayList::new));
            packetShapeless.result = ItemStack_1_13_1.fromMinecraft(shapeless.getResultItem());
            packetRecipe = packetShapeless;
        } else {
            packetRecipe = new SPacketSynchronizeRecipes_1_13_2.Special();
        }
        packetRecipe.type = PacketSystem.clientIdToServer(Registry.RECIPE_SERIALIZER, Registry.RECIPE_SERIALIZER.getKey(recipe.getSerializer())).getPath();

        var result = new SPacketSynchronizeRecipes_1_13_2.RecipeWithId();
        result.recipeId = id;
        result.recipe = packetRecipe;
        return result;
    }

    private static SPacketSynchronizeRecipes_Latest.Ingredient convertIngredient(Ingredient ingredient) {
        var result = new SPacketSynchronizeRecipes_Latest.Ingredient();
        result.options = new ArrayList<>(ingredient.getItems().length);
        for (ItemStack matchingStack : ingredient.getItems()) {
            result.options.add(ItemStack_1_13_1.fromMinecraft(matchingStack));
        }
        return result;
    }
}
