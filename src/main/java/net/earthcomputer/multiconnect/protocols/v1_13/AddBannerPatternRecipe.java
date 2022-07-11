package net.earthcomputer.multiconnect.protocols.v1_13;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerBlockEntity;

public class AddBannerPatternRecipe extends CustomRecipe {
    public static final RecipeSerializer<AddBannerPatternRecipe> SERIALIZER = new SimpleRecipeSerializer<>(AddBannerPatternRecipe::new);

    public AddBannerPatternRecipe(ResourceLocation output) {
        super(output);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level world) {
        boolean foundBanner = false;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
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
    public ItemStack assemble(CraftingContainer inv) {
        ItemStack result = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BannerItem) {
                result = stack.copy();
                result.setCount(1);
                break;
            }
        }

        BannerPattern_1_13_2 pattern = getBannerPattern(inv);
        if (pattern != null) {
            DyeColor color = ConnectionInfo.protocolVersion <= Protocols.V1_12_2 ? DyeColor.BLACK : DyeColor.WHITE;
            for (int i = 0; i < inv.getContainerSize(); i++) {
                Item item = inv.getItem(i).getItem();
                if (item instanceof DyeItem dyeItem) {
                    color = dyeItem.getDyeColor();
                }
            }

            CompoundTag tileEntityNbt = result.getOrCreateTagElement("BlockEntityTag");
            ListTag patterns;
            if (tileEntityNbt.contains("Patterns", 9)) {
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
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<AddBannerPatternRecipe> getSerializer() {
        return SERIALIZER;
    }

    private static BannerPattern_1_13_2 getBannerPattern(CraftingContainer inv) {
        for (BannerPattern_1_13_2 pattern : BannerPattern_1_13_2.values()) {
            if (!pattern.isCraftable())
                continue;

            boolean matches = true;
            if (pattern.hasBaseStack()) {
                boolean foundBaseItem = false;
                boolean foundDye = false;
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    ItemStack stack = inv.getItem(i);
                    if (!stack.isEmpty() && !(stack.getItem() instanceof BannerItem)) {
                        if (stack.getItem() instanceof DyeItem) {
                            if (foundDye) {
                                matches = false;
                                break;
                            }
                            foundDye = true;
                        } else {
                            if (foundBaseItem || !stack.sameItemStackIgnoreDurability(pattern.getBaseStack())) {
                                matches = false;
                                break;
                            }
                            foundBaseItem = true;
                        }
                    }
                }
                if (!foundBaseItem || (!foundDye && ConnectionInfo.protocolVersion > Protocols.V1_10))
                    matches = false;
            } else if (inv.getContainerSize() == pattern.getRecipePattern().length * pattern.getRecipePattern()[0].length()) {
                DyeColor patternColor = null;
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    int row = i / 3;
                    int col = i % 3;
                    ItemStack stack = inv.getItem(i);
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

    public static void register() {
        Registry.register(Registry.RECIPE_SERIALIZER, "multiconnect:crafting_special_banneraddpattern", SERIALIZER);
    }
}
