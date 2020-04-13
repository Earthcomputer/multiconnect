package net.earthcomputer.multiconnect.protocols.v1_12_2;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class RecipeInfo<T extends IRecipe<?>> {

    private final Function<ResourceLocation, IRecipe<?>> creator;
    private final IRecipeSerializer<T> recipeType;
    private final ItemStack output;

    private RecipeInfo(Function<ResourceLocation, IRecipe<?>> creator, IRecipeSerializer<T> recipeType, ItemStack output) {
        this.creator = creator;
        this.recipeType = recipeType;
        this.output = output;
    }

    public static <T extends IRecipe<?>> RecipeInfo<T> of(Function<ResourceLocation, IRecipe<?>> creator, IRecipeSerializer<T> recipeType, ItemStack output) {
        return new RecipeInfo<>(creator, recipeType, output);
    }

    public static <T extends IRecipe<?>> RecipeInfo<T> of(Function<ResourceLocation, IRecipe<?>> creator, IRecipeSerializer<T> recipeType, IItemProvider output) {
        return of(creator, recipeType, new ItemStack(output));
    }

    public static <T extends IRecipe<?>> RecipeInfo<T> of(Function<ResourceLocation, IRecipe<?>> creator, IRecipeSerializer<T> recipeType, IItemProvider output, int count) {
        return of(creator, recipeType, new ItemStack(output, count));
    }

    public static RecipeInfo<ShapedRecipe> shaped(ItemStack output, Object... args) {
        return shaped("", output, args);
    }

    public static RecipeInfo<ShapedRecipe> shaped(IItemProvider output, Object... args) {
        return shaped("", output, args);
    }

    public static RecipeInfo<ShapedRecipe> shaped(int count, IItemProvider output, Object... args) {
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
            List<IItemProvider> items = new ArrayList<>();
            for (; i < args.length && args[i] instanceof IItemProvider; i++) {
                items.add((IItemProvider) args[i]);
            }
            legend.put(key, Ingredient.fromItems(items.toArray(new IItemProvider[0])));
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
        return new RecipeInfo<>(id -> new ShapedRecipe(id, group, width_f, height, ingredients, output), IRecipeSerializer.CRAFTING_SHAPED, output);
    }

    public static RecipeInfo<ShapedRecipe> shaped(String group, IItemProvider output, Object... args) {
        return shaped(group, new ItemStack(output), args);
    }

    public static RecipeInfo<ShapedRecipe> shaped(String group, int count, IItemProvider output, Object... args) {
        return shaped(group, new ItemStack(output, count), args);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(String group, ItemStack output, IItemProvider... inputs) {
        IItemProvider[][] newInputs = new IItemProvider[inputs.length][1];
        for (int i = 0; i < inputs.length; i++)
            newInputs[i] = new IItemProvider[] {inputs[i]};
        return shapeless(group, output, newInputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(String group, IItemProvider output, IItemProvider... inputs) {
        return shapeless(group, new ItemStack(output), inputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(String group, int count, IItemProvider output, IItemProvider... inputs) {
        return shapeless(group, new ItemStack(output, count), inputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(String group, ItemStack output, IItemProvider[]... inputs) {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (IItemProvider[] input : inputs) {
            ingredients.add(Ingredient.fromItems(input));
        }
        return new RecipeInfo<>(id -> new ShapelessRecipe(id, group, output, ingredients), IRecipeSerializer.CRAFTING_SHAPELESS, output);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(String group, IItemProvider output, IItemProvider[]... inputs) {
        return shapeless(group, new ItemStack(output), inputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(String group, int count, IItemProvider output, IItemProvider[]... inputs) {
        return shapeless(group, new ItemStack(output, count), inputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(ItemStack output, IItemProvider... inputs) {
        return shapeless("", output, inputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(IItemProvider output, IItemProvider... inputs) {
        return shapeless("", output, inputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(int count, IItemProvider output, IItemProvider... inputs) {
        return shapeless("", count, output, inputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(ItemStack output, IItemProvider[]... inputs) {
        return shapeless("", output, inputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(IItemProvider output, IItemProvider[]... inputs) {
        return shapeless("", output, inputs);
    }

    public static RecipeInfo<ShapelessRecipe> shapeless(int count, IItemProvider output, IItemProvider[]... inputs) {
        return shapeless("", count, output, inputs);
    }


    public IRecipe<?> create(ResourceLocation id) {
        return creator.apply(id);
    }

    public IRecipeSerializer<T> getRecipeType() {
        return recipeType;
    }

    public ItemStack getOutput() {
        return output;
    }
}
