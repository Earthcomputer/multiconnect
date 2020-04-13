package net.earthcomputer.multiconnect.protocols.v1_13_2;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class AddBannerPatternRecipe extends SpecialRecipe {

    public static final IRecipeSerializer<AddBannerPatternRecipe> SERIALIZER = new SpecialRecipeSerializer<>(AddBannerPatternRecipe::new);

    public AddBannerPatternRecipe(ResourceLocation output) {
        super(output);
    }

    @Override
    public boolean matches(CraftingInventory inv, World world) {
        boolean foundBanner = false;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.getItem() instanceof BannerItem) {
                if (foundBanner)
                    return false;
                if (BannerTileEntity.getPatterns(stack) >= 6)
                    return false;
                foundBanner = true;
            }
        }
        return foundBanner && getBannerPattern(inv) != null;
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        ItemStack result = ItemStack.EMPTY;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BannerItem) {
                result = stack.copy();
                result.setCount(1);
                break;
            }
        }

        BannerPattern_1_13_2 pattern = getBannerPattern(inv);
        if (pattern != null) {
            DyeColor color = DyeColor.WHITE;
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                Item item = inv.getStackInSlot(i).getItem();
                if (item instanceof DyeItem) {
                    color = ((DyeItem) item).getDyeColor();
                }
            }

            CompoundNBT tileEntityNbt = result.getOrCreateChildTag("BlockEntityTag");
            ListNBT patterns;
            if (tileEntityNbt.contains("Patterns", 9)) {
                patterns = tileEntityNbt.getList("Patterns", 10);
            } else {
                patterns = new ListNBT();
                tileEntityNbt.put("Patterns", patterns);
            }
            CompoundNBT patternNbt = new CompoundNBT();
            patternNbt.putString("Pattern", pattern.getId());
            patternNbt.putInt("Color", color.getId());
            patterns.add(patternNbt);
        }

        return result;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public IRecipeSerializer<AddBannerPatternRecipe> getSerializer() {
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
                for (int i = 0; i < inv.getSizeInventory(); i++) {
                    ItemStack stack = inv.getStackInSlot(i);
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
            } else if (inv.getSizeInventory() == pattern.getRecipePattern().length * pattern.getRecipePattern()[0].length()) {
                DyeColor patternColor = null;
                for (int i = 0; i < inv.getSizeInventory(); i++) {
                    int row = i / 3;
                    int col = i % 3;
                    ItemStack stack = inv.getStackInSlot(i);
                    Item item = stack.getItem();
                    if (!stack.isEmpty() && !(item instanceof BannerItem)) {
                        if (!(item instanceof DyeItem)) {
                            matches = false;
                            break;
                        }

                        DyeColor color = ((DyeItem) item).getDyeColor();
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
