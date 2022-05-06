package net.earthcomputer.multiconnect.protocols.v1_17_1;

import net.earthcomputer.multiconnect.protocols.generic.Key;
import net.earthcomputer.multiconnect.protocols.generic.TagRegistry;
import net.earthcomputer.multiconnect.protocols.v1_18.Protocol_1_18;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.ItemTags;

import java.util.BitSet;

public class Protocol_1_17_1 extends Protocol_1_18 {
    public static final Key<BitSet> VERTICAL_STRIP_BITMASK = Key.create("verticalStripBitmask");
    private static final Key<int[]> BIOMES = Key.create("biomes");

    @Override
    public void addExtraBlockTags(TagRegistry<Block> tags) {
        tags.add(BlockTags.ANIMALS_SPAWNABLE_ON, Blocks.GRASS_BLOCK);
        tags.add(BlockTags.AXOLOTLS_SPAWNABLE_ON, Blocks.CLAY);
        tags.add(BlockTags.TERRACOTTA, Blocks.TERRACOTTA, Blocks.WHITE_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA,
                Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA, Blocks.LIME_TERRACOTTA, Blocks.PINK_TERRACOTTA,
                Blocks.GRAY_TERRACOTTA, Blocks.LIGHT_GRAY_TERRACOTTA, Blocks.CYAN_TERRACOTTA, Blocks.PURPLE_TERRACOTTA,
                Blocks.BLUE_TERRACOTTA, Blocks.BROWN_TERRACOTTA, Blocks.GREEN_TERRACOTTA, Blocks.RED_TERRACOTTA,
                Blocks.BLACK_TERRACOTTA);
        tags.addTag(BlockTags.AZALEA_GROWS_ON, BlockTags.DIRT);
        tags.addTag(BlockTags.AZALEA_GROWS_ON, BlockTags.SAND);
        tags.addTag(BlockTags.AZALEA_GROWS_ON, BlockTags.TERRACOTTA);
        tags.add(BlockTags.AZALEA_GROWS_ON, Blocks.SNOW_BLOCK, Blocks.POWDER_SNOW);
        tags.addTag(BlockTags.AZALEA_ROOT_REPLACEABLE, BlockTags.LUSH_GROUND_REPLACEABLE);
        tags.addTag(BlockTags.AZALEA_ROOT_REPLACEABLE, BlockTags.TERRACOTTA);
        tags.add(BlockTags.AZALEA_ROOT_REPLACEABLE, Blocks.RED_SAND);
        tags.addTag(BlockTags.BIG_DRIPLEAF_PLACEABLE, BlockTags.SMALL_DRIPLEAF_PLACEABLE);
        tags.addTag(BlockTags.BIG_DRIPLEAF_PLACEABLE, BlockTags.DIRT);
        tags.add(BlockTags.BIG_DRIPLEAF_PLACEABLE, Blocks.FARMLAND);
        tags.add(BlockTags.FOXES_SPAWNABLE_ON, Blocks.GRASS_BLOCK, Blocks.SNOW, Blocks.SNOW_BLOCK, Blocks.PODZOL,
                Blocks.COARSE_DIRT);
        tags.add(BlockTags.GOATS_SPAWNABLE_ON, Blocks.STONE, Blocks.SNOW, Blocks.POWDER_SNOW, Blocks.SNOW_BLOCK,
                Blocks.PACKED_ICE, Blocks.GRAVEL);
        tags.add(BlockTags.MOOSHROOMS_SPAWNABLE_ON, Blocks.MYCELIUM);
        tags.add(BlockTags.PARROTS_SPAWNABLE_ON, Blocks.GRASS_BLOCK, Blocks.AIR);
        tags.addTag(BlockTags.PARROTS_SPAWNABLE_ON, BlockTags.LEAVES);
        tags.addTag(BlockTags.PARROTS_SPAWNABLE_ON, BlockTags.LOGS);
        tags.add(BlockTags.POLAR_BEARS_SPAWNABLE_ON_IN_FROZEN_OCEAN, Blocks.ICE);
        tags.add(BlockTags.RABBITS_SPAWNABLE_ON, Blocks.GRASS_BLOCK, Blocks.SNOW, Blocks.SNOW_BLOCK, Blocks.SAND);
        tags.add(BlockTags.REPLACEABLE_PLANTS, Blocks.GRASS, Blocks.FERN, Blocks.DEAD_BUSH, Blocks.VINE,
                Blocks.GLOW_LICHEN, Blocks.SUNFLOWER, Blocks.LILAC, Blocks.ROSE_BUSH,
                Blocks.PEONY, Blocks.TALL_GRASS, Blocks.LARGE_FERN, Blocks.HANGING_ROOTS);
        tags.add(BlockTags.WOLVES_SPAWNABLE_ON, Blocks.GRASS_BLOCK, Blocks.SNOW, Blocks.SNOW_BLOCK);
        tags.addTag(BlockTags.LAVA_POOL_STONE_CANNOT_REPLACE, BlockTags.FEATURES_CANNOT_REPLACE);
        tags.addTag(BlockTags.LAVA_POOL_STONE_CANNOT_REPLACE, BlockTags.LEAVES);
        tags.addTag(BlockTags.LAVA_POOL_STONE_CANNOT_REPLACE, BlockTags.LOGS);
        super.addExtraBlockTags(tags);
    }

    @Override
    public void addExtraItemTags(TagRegistry<Item> tags, TagRegistry<Block> blockTags) {
        copyBlocks(tags, blockTags, ItemTags.DIRT, BlockTags.DIRT);
        copyBlocks(tags, blockTags, ItemTags.TERRACOTTA, BlockTags.TERRACOTTA);
        super.addExtraItemTags(tags, blockTags);
    }
}
