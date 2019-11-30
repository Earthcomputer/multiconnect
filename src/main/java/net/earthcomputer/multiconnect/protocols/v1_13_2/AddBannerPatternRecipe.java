package net.earthcomputer.multiconnect.protocols.v1_13_2;

import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.BannerItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class AddBannerPatternRecipe extends SpecialCraftingRecipe {

    public static final RecipeSerializer<AddBannerPatternRecipe> SERIALIZER = new SpecialRecipeSerializer<>(AddBannerPatternRecipe::new);

    public AddBannerPatternRecipe(Identifier output) {
        super(output);
    }

    @Override
    public boolean matches(CraftingInventory inv, World world) {
        boolean foundBanner = false;
        for (int i = 0; i < inv.getInvSize(); i++) {
            ItemStack stack = inv.getInvStack(i);
            if (stack.getItem() instanceof BannerItem) {
                if (foundBanner)
                    return false;
                if (BannerBlockEntity.getPatternCount(stack) >= 6)
                    return false;
                foundBanner = true;
            }
        }
        return foundBanner && getBannerPattern(inv) != null;
    }

    @Override
    public ItemStack craft(CraftingInventory inv) {
        ItemStack result = ItemStack.EMPTY;

        for (int i = 0; i < inv.getInvSize(); i++) {
            ItemStack stack = inv.getInvStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BannerItem) {
                result = stack.copy();
                result.setCount(1);
                break;
            }
        }

        BannerPattern_1_13_2 pattern = getBannerPattern(inv);
        if (pattern != null) {
            DyeColor color = DyeColor.WHITE;
            for (int i = 0; i < inv.getInvSize(); i++) {
                Item item = inv.getInvStack(i).getItem();
                if (item instanceof DyeItem) {
                    color = ((DyeItem) item).getColor();
                }
            }

            CompoundTag tileEntityNbt = result.getOrCreateSubTag("BlockEntityTag");
            ListTag patterns;
            if (tileEntityNbt.containsKey("Patterns", 9)) {
                patterns = tileEntityNbt.getList("Patterns", 10);
            } else {
                patterns = new ListTag();
                tileEntityNbt.put("Patterns", patterns);
            }
            CompoundTag patternNbt = new CompoundTag();
            patternNbt.putString("Pattern", pattern.getId());
            patternNbt.putInt("Color", color.getId());
            patterns.add(patternNbt);
        }

        return result;
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<AddBannerPatternRecipe> getSerializer() {
        return SERIALIZER;
    }

    private static BannerPattern_1_13_2 getBannerPattern(CraftingInventory inv) {
        for (BannerPattern_1_13_2 pattern : BannerPattern_1_13_2.values()) {
            if (!pattern.isCraftable())
                continue;

            boolean matches = true;
            if (pattern.hasBaseStack()) {
                boolean foundBaseItem = false;
                boolean foundDye = false;
                for (int i = 0; i < inv.getInvSize(); i++) {
                    ItemStack stack = inv.getInvStack(i);
                    if (!stack.isEmpty() && !(stack.getItem() instanceof BannerItem)) {
                        if (stack.getItem() instanceof DyeItem) {
                            if (foundDye) {
                                matches = false;
                                break;
                            }
                            foundDye = true;
                        } else {
                            if (foundBaseItem || !stack.isItemEqual(pattern.getBaseStack())) {
                                matches = false;
                                break;
                            }
                            foundBaseItem = true;
                        }
                    }
                }
                if (!foundBaseItem || !foundDye)
                    matches = false;
            } else if (inv.getInvSize() == pattern.getRecipePattern().length * pattern.getRecipePattern()[0].length()) {
                DyeColor patternColor = null;
                for (int i = 0; i < inv.getInvSize(); i++) {
                    int row = i / 3;
                    int col = i % 3;
                    ItemStack stack = inv.getInvStack(i);
                    Item item = stack.getItem();
                    if (!stack.isEmpty() && !(item instanceof BannerItem)) {
                        if (!(item instanceof DyeItem)) {
                            matches = false;
                            break;
                        }

                        DyeColor color = ((DyeItem) item).getColor();
                        if (patternColor != null && color != patternColor) {
                            matches = false;
                            break;
                        }

                        if (pattern.getRecipePattern()[row].charAt(col) == ' ') {
                            matches = false;
                            break;
                        }

                        patternColor = color;
                    } else if (pattern.getRecipePattern()[row].charAt(col) != ' ') {
                        matches = false;
                        break;
                    }
                }
            } else {
                matches = false;
            }

            if (matches)
                return pattern;
        }

        return null;
    }
}
