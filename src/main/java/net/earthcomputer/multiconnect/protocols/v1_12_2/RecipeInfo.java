package net.earthcomputer.multiconnect.protocols.v1_12_2;

import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class RecipeInfo<T extends Recipe<?>> {

    private final Function<Identifier, Recipe<?>> creator;
    private final RecipeSerializer<T> recipeType;
    private final ItemStack output;

    private RecipeInfo(Function<Identifier, Recipe<?>> creator, RecipeSerializer<T> recipeType, ItemStack output) {
        this.creator = creator;
        this.recipeType = recipeType;
        this.output = output;
    }

    public static <T extends Recipe<?>> RecipeInfo<T> of(Function<Identifier, Recipe<?>> creator, RecipeSerializer<T> recipeType, ItemStack output) {
        return new RecipeInfo<>(creator, recipeType, output);
    }

    public static <T extends Recipe<?>> RecipeInfo<T> of(Function<Identifier, Recipe<?>> creator, RecipeSerializer<T> recipeType, ItemConvertible output) {
        return of(creator, recipeType, new ItemStack(output));
    }

    public static <T extends Recipe<?>> RecipeInfo<T> of(Function<Identifier, Recipe<?>> creator, RecipeSerializer<T> recipeType, ItemConvertible output, int count) {
        return of(creator, recipeType, new ItemStack(output, count));
    }

    public static RecipeInfo<ShapedRecipe> shaped(ItemStack output, Object... args) {
        return shaped("", output, args);
    }

    public static RecipeInfo<ShapedRecipe> shaped(ItemConvertible output, Object... args) {
        return shaped("", output, args);
    }

    public static RecipeInfo<ShapedRecipe> shaped(int count, ItemConvertible output, Object... args) {
        return shaped("", count, output, args);
    }

    public static RecipeInfo<ShapedRecipe> shaped(String group, ItemStack output, Object... args) {
        int i;
        int width = 0;
        List<String> shape = new ArrayList<>();
        for (i = 0; i < args.length && args[i] instanceof String; i++) {
            String str = (String) args[i];
            if (i == 0)
                width = str.length();
            else if (str.length() != width)
                throw new IllegalArgumentException("Rows do not have consistent width");
            shape.add(str);
        }
        Map<Character, Ingredient> legend = new HashMap<>();
        while (i < args.length && args[i] instanceof Character) {
            Character key = (Character) args[i];
            i++;
            List<ItemConvertible> items = new ArrayList<>();
            for (; i < args.length && args[i] instanceof ItemConvertible; i++) {
                items.add((ItemConvertible) args[i]);
            }
            legend.put(key, Ingredient.ofItems(items.toArray(new ItemConvertible[0])));
        }
        if (i != args.length)
            throw new IllegalArgumentException("Unexpected argument at index " + i + ": " + args[i]);

        int height = shape.size();
        DefaultedList<Ingredient> ingredients = DefaultedList.of();
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
        return new RecipeInfo<>(id -> new ShapedRecipe(id, group, width_f, height, ingredients, output), RecipeSerializer.SHAPED, output);
    }

    public static RecipeInfo<ShapedRecipe> shaped(String group, ItemConvertible output, Object... args) {
        return shaped(group, new ItemStack(output), args);
    }

    public static RecipeInfo<ShapedRecipe> shaped(String group, int count, ItemConvertible output, Object... args) {
        return shaped(group, new ItemStack(output, count), args);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(String group, ItemStack output, ItemConvertible... inputs) {
        ItemConvertible[][] newInputs = new ItemConvertible[inputs.length][1];
        for (int i = 0; i < inputs.length; i++)
            newInputs[i] = new ItemConvertible[] {inputs[i]};
        return shapeless(group, output, newInputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(String group, ItemConvertible output, ItemConvertible... inputs) {
        return shapeless(group, new ItemStack(output), inputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(String group, int count, ItemConvertible output, ItemConvertible... inputs) {
        return shapeless(group, new ItemStack(output, count), inputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(String group, ItemStack output, ItemConvertible[]... inputs) {
        DefaultedList<Ingredient> ingredients = DefaultedList.of();
        for (ItemConvertible[] input : inputs) {
            ingredients.add(Ingredient.ofItems(input));
        }
        return new RecipeInfo<>(id -> new ShapelessRecipe(id, group, output, ingredients), RecipeSerializer.SHAPELESS, output);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(String group, ItemConvertible output, ItemConvertible[]... inputs) {
        return shapeless(group, new ItemStack(output), inputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(String group, int count, ItemConvertible output, ItemConvertible[]... inputs) {
        return shapeless(group, new ItemStack(output, count), inputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(ItemStack output, ItemConvertible... inputs) {
        return shapeless("", output, inputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(ItemConvertible output, ItemConvertible... inputs) {
        return shapeless("", output, inputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(int count, ItemConvertible output, ItemConvertible... inputs) {
        return shapeless("", count, output, inputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(ItemStack output, ItemConvertible[]... inputs) {
        return shapeless("", output, inputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(ItemConvertible output, ItemConvertible[]... inputs) {
        return shapeless("", output, inputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(int count, ItemConvertible output, ItemConvertible[]... inputs) {
        return shapeless("", count, output, inputs);
    }


    public Recipe<?> create(Identifier id) {
        return creator.apply(id);
    }

    public RecipeSerializer<T> getRecipeType() {
        return recipeType;
    }

    public ItemStack getOutput() {
        return output;
    }
}
