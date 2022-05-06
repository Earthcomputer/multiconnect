package net.earthcomputer.multiconnect.protocols.v1_16_5;

import net.earthcomputer.multiconnect.protocols.generic.*;
import net.earthcomputer.multiconnect.protocols.v1_16_5.mixin.EntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_16_5.mixin.ShulkerEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_17.Protocol_1_17;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tag.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;

import java.util.*;

public class Protocol_1_16_5 extends Protocol_1_17 {
    public static final int BIOME_ARRAY_LENGTH = 1024;
    private static short lastActionId = 0;

    private static final TrackedData<Optional<BlockPos>> OLD_SHULKER_ATTACHED_POSITION = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);

    public static final Key<Boolean> FULL_CHUNK_KEY = Key.create("fullChunk", true);

    public static short getLastScreenActionId() {
        return lastActionId;
    }

    public static short nextScreenActionId() {
        return ++lastActionId;
    }

    @Override
    public float getBlockHardness(BlockState state, float hardness) {
        if (state.getBlock() instanceof InfestedBlock) {
            return 0;
        }
        return super.getBlockHardness(state, hardness);
    }

    @Override
    public float getBlockResistance(Block block, float resistance) {
        if (block instanceof InfestedBlock) {
            return 0.75f;
        }
        return super.getBlockResistance(block, resistance);
    }

    @Override
    public void addExtraBlockTags(TagRegistry<Block> tags) {
        tags.add(BlockTags.CANDLES);
        tags.add(BlockTags.CANDLE_CAKES);
        tags.add(BlockTags.CAULDRONS, Blocks.CAULDRON, Blocks.WATER_CAULDRON);
        tags.add(BlockTags.CRYSTAL_SOUND_BLOCKS);
        tags.add(BlockTags.INSIDE_STEP_SOUND_BLOCKS, Blocks.SNOW);
        tags.addTag(BlockTags.DRIPSTONE_REPLACEABLE_BLOCKS, BlockTags.BASE_STONE_OVERWORLD);
        tags.add(BlockTags.DRIPSTONE_REPLACEABLE_BLOCKS, Blocks.DIRT);
        tags.addTag(BlockTags.OCCLUDES_VIBRATION_SIGNALS, BlockTags.WOOL);
        tags.add(BlockTags.CAVE_VINES);
        tags.addTag(BlockTags.MOSS_REPLACEABLE, BlockTags.BASE_STONE_OVERWORLD);
        tags.addTag(BlockTags.MOSS_REPLACEABLE, BlockTags.CAVE_VINES);
        tags.add(BlockTags.MOSS_REPLACEABLE, Blocks.DIRT);
        tags.addTag(BlockTags.LUSH_GROUND_REPLACEABLE, BlockTags.MOSS_REPLACEABLE);
        tags.add(BlockTags.LUSH_GROUND_REPLACEABLE, Blocks.CLAY, Blocks.GRAVEL, Blocks.SAND);
        tags.add(BlockTags.IRON_ORES, Blocks.IRON_ORE);
        tags.add(BlockTags.DIAMOND_ORES, Blocks.DIAMOND_ORE);
        tags.add(BlockTags.REDSTONE_ORES, Blocks.REDSTONE_ORE);
        tags.add(BlockTags.LAPIS_ORES, Blocks.LAPIS_ORE);
        tags.add(BlockTags.COAL_ORES, Blocks.COAL_ORE);
        tags.add(BlockTags.EMERALD_ORES, Blocks.EMERALD_ORE);
        tags.add(BlockTags.COPPER_ORES);
        tags.add(BlockTags.STONE_ORE_REPLACEABLES, Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE);
        tags.add(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        tags.add(BlockTags.DIRT, Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.PODZOL, Blocks.COARSE_DIRT, Blocks.MYCELIUM);
        tags.add(BlockTags.SNOW, Blocks.SNOW, Blocks.SNOW_BLOCK);
        tags.add(BlockTags.SMALL_DRIPLEAF_PLACEABLE, Blocks.CLAY);
        tags.add(BlockTags.AXE_MINEABLE,
                Blocks.NOTE_BLOCK, Blocks.ATTACHED_MELON_STEM, Blocks.ATTACHED_PUMPKIN_STEM, Blocks.BAMBOO, Blocks.BARREL,
                Blocks.BEE_NEST, Blocks.BEEHIVE, Blocks.BEETROOTS, Blocks.BOOKSHELF, Blocks.BROWN_MUSHROOM_BLOCK,
                Blocks.BROWN_MUSHROOM, Blocks.CAMPFIRE, Blocks.CARROTS, Blocks.CARTOGRAPHY_TABLE, Blocks.CARVED_PUMPKIN,
                Blocks.CHEST, Blocks.CHORUS_FLOWER, Blocks.CHORUS_PLANT, Blocks.COCOA, Blocks.COMPOSTER,
                Blocks.CRAFTING_TABLE, Blocks.CRIMSON_FUNGUS, Blocks.DAYLIGHT_DETECTOR, Blocks.DEAD_BUSH, Blocks.FERN,
                Blocks.FLETCHING_TABLE, Blocks.GLOW_LICHEN, Blocks.GRASS, Blocks.JACK_O_LANTERN, Blocks.JUKEBOX,
                Blocks.LADDER, Blocks.LARGE_FERN, Blocks.LECTERN, Blocks.LILY_PAD, Blocks.LOOM,
                Blocks.MELON_STEM, Blocks.MELON, Blocks.MUSHROOM_STEM, Blocks.NETHER_WART, Blocks.POTATOES,
                Blocks.PUMPKIN_STEM, Blocks.PUMPKIN, Blocks.RED_MUSHROOM_BLOCK, Blocks.RED_MUSHROOM, Blocks.SCAFFOLDING,
                Blocks.SMITHING_TABLE, Blocks.SOUL_CAMPFIRE, Blocks.SUGAR_CANE, Blocks.SWEET_BERRY_BUSH, Blocks.TALL_GRASS,
                Blocks.TRAPPED_CHEST, Blocks.TWISTING_VINES_PLANT, Blocks.TWISTING_VINES, Blocks.VINE, Blocks.WARPED_FUNGUS,
                Blocks.WEEPING_VINES_PLANT, Blocks.WEEPING_VINES, Blocks.WHEAT);
        tags.addTag(BlockTags.AXE_MINEABLE, BlockTags.BANNERS);
        tags.addTag(BlockTags.AXE_MINEABLE, BlockTags.FENCE_GATES);
        tags.addTag(BlockTags.AXE_MINEABLE, BlockTags.FLOWERS);
        tags.addTag(BlockTags.AXE_MINEABLE, BlockTags.LOGS);
        tags.addTag(BlockTags.AXE_MINEABLE, BlockTags.PLANKS);
        tags.addTag(BlockTags.AXE_MINEABLE, BlockTags.SAPLINGS);
        tags.addTag(BlockTags.AXE_MINEABLE, BlockTags.SIGNS);
        tags.addTag(BlockTags.AXE_MINEABLE, BlockTags.WOODEN_BUTTONS);
        tags.addTag(BlockTags.AXE_MINEABLE, BlockTags.WOODEN_DOORS);
        tags.addTag(BlockTags.AXE_MINEABLE, BlockTags.WOODEN_FENCES);
        tags.addTag(BlockTags.AXE_MINEABLE, BlockTags.WOODEN_PRESSURE_PLATES);
        tags.addTag(BlockTags.AXE_MINEABLE, BlockTags.WOODEN_SLABS);
        tags.addTag(BlockTags.AXE_MINEABLE, BlockTags.WOODEN_STAIRS);
        tags.addTag(BlockTags.AXE_MINEABLE, BlockTags.WOODEN_TRAPDOORS);
        tags.add(BlockTags.HOE_MINEABLE,
                Blocks.NETHER_WART_BLOCK, Blocks.WARPED_WART_BLOCK, Blocks.HAY_BLOCK, Blocks.DRIED_KELP_BLOCK, Blocks.TARGET,
                Blocks.SPONGE, Blocks.WET_SPONGE, Blocks.JUNGLE_LEAVES, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES,
                Blocks.DARK_OAK_LEAVES, Blocks.ACACIA_LEAVES, Blocks.BIRCH_LEAVES);
        tags.add(BlockTags.PICKAXE_MINEABLE,
                Blocks.STONE, Blocks.GRANITE, Blocks.POLISHED_GRANITE, Blocks.DIORITE, Blocks.POLISHED_DIORITE,
                Blocks.ANDESITE, Blocks.POLISHED_ANDESITE, Blocks.COBBLESTONE, Blocks.GOLD_ORE, Blocks.IRON_ORE,
                Blocks.COAL_ORE, Blocks.NETHER_GOLD_ORE, Blocks.LAPIS_ORE, Blocks.LAPIS_BLOCK, Blocks.DISPENSER,
                Blocks.SANDSTONE, Blocks.CHISELED_SANDSTONE, Blocks.CUT_SANDSTONE, Blocks.GOLD_BLOCK, Blocks.IRON_BLOCK,
                Blocks.BRICKS, Blocks.MOSSY_COBBLESTONE, Blocks.OBSIDIAN, Blocks.SPAWNER, Blocks.DIAMOND_ORE,
                Blocks.DIAMOND_BLOCK, Blocks.FURNACE, Blocks.COBBLESTONE_STAIRS, Blocks.STONE_PRESSURE_PLATE, Blocks.IRON_DOOR,
                Blocks.REDSTONE_ORE, Blocks.NETHERRACK, Blocks.BASALT, Blocks.POLISHED_BASALT, Blocks.STONE_BRICKS,
                Blocks.MOSSY_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS, Blocks.CHISELED_STONE_BRICKS, Blocks.IRON_BARS, Blocks.CHAIN,
                Blocks.BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_BRICK_STAIRS,
                Blocks.ENCHANTING_TABLE, Blocks.BREWING_STAND, Blocks.END_STONE, Blocks.SANDSTONE_STAIRS, Blocks.EMERALD_ORE,
                Blocks.ENDER_CHEST, Blocks.EMERALD_BLOCK, Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, Blocks.REDSTONE_BLOCK,
                Blocks.NETHER_QUARTZ_ORE, Blocks.HOPPER, Blocks.QUARTZ_BLOCK, Blocks.CHISELED_QUARTZ_BLOCK, Blocks.QUARTZ_PILLAR,
                Blocks.QUARTZ_STAIRS, Blocks.DROPPER, Blocks.WHITE_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA,
                Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA, Blocks.LIME_TERRACOTTA, Blocks.PINK_TERRACOTTA, Blocks.GRAY_TERRACOTTA,
                Blocks.LIGHT_GRAY_TERRACOTTA, Blocks.CYAN_TERRACOTTA, Blocks.PURPLE_TERRACOTTA, Blocks.BLUE_TERRACOTTA, Blocks.BROWN_TERRACOTTA,
                Blocks.GREEN_TERRACOTTA, Blocks.RED_TERRACOTTA, Blocks.BLACK_TERRACOTTA, Blocks.IRON_TRAPDOOR, Blocks.PRISMARINE,
                Blocks.PRISMARINE_BRICKS, Blocks.DARK_PRISMARINE, Blocks.PRISMARINE_STAIRS, Blocks.PRISMARINE_BRICK_STAIRS, Blocks.DARK_PRISMARINE_STAIRS,
                Blocks.PRISMARINE_SLAB, Blocks.PRISMARINE_BRICK_SLAB, Blocks.DARK_PRISMARINE_SLAB, Blocks.TERRACOTTA, Blocks.COAL_BLOCK,
                Blocks.RED_SANDSTONE, Blocks.CHISELED_RED_SANDSTONE, Blocks.CUT_RED_SANDSTONE, Blocks.RED_SANDSTONE_STAIRS, Blocks.STONE_SLAB,
                Blocks.SMOOTH_STONE_SLAB, Blocks.SANDSTONE_SLAB, Blocks.CUT_SANDSTONE_SLAB, Blocks.PETRIFIED_OAK_SLAB, Blocks.COBBLESTONE_SLAB,
                Blocks.BRICK_SLAB, Blocks.STONE_BRICK_SLAB, Blocks.NETHER_BRICK_SLAB, Blocks.QUARTZ_SLAB, Blocks.RED_SANDSTONE_SLAB,
                Blocks.CUT_RED_SANDSTONE_SLAB, Blocks.PURPUR_SLAB, Blocks.SMOOTH_STONE, Blocks.SMOOTH_SANDSTONE, Blocks.SMOOTH_QUARTZ,
                Blocks.SMOOTH_RED_SANDSTONE, Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR, Blocks.PURPUR_STAIRS, Blocks.END_STONE_BRICKS,
                Blocks.MAGMA_BLOCK, Blocks.RED_NETHER_BRICKS, Blocks.BONE_BLOCK, Blocks.OBSERVER, Blocks.WHITE_GLAZED_TERRACOTTA,
                Blocks.ORANGE_GLAZED_TERRACOTTA, Blocks.MAGENTA_GLAZED_TERRACOTTA, Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA, Blocks.YELLOW_GLAZED_TERRACOTTA, Blocks.LIME_GLAZED_TERRACOTTA,
                Blocks.PINK_GLAZED_TERRACOTTA, Blocks.GRAY_GLAZED_TERRACOTTA, Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA, Blocks.CYAN_GLAZED_TERRACOTTA, Blocks.PURPLE_GLAZED_TERRACOTTA,
                Blocks.BLUE_GLAZED_TERRACOTTA, Blocks.BROWN_GLAZED_TERRACOTTA, Blocks.GREEN_GLAZED_TERRACOTTA, Blocks.RED_GLAZED_TERRACOTTA, Blocks.BLACK_GLAZED_TERRACOTTA,
                Blocks.WHITE_CONCRETE, Blocks.ORANGE_CONCRETE, Blocks.MAGENTA_CONCRETE, Blocks.LIGHT_BLUE_CONCRETE, Blocks.YELLOW_CONCRETE,
                Blocks.LIME_CONCRETE, Blocks.PINK_CONCRETE, Blocks.GRAY_CONCRETE, Blocks.LIGHT_GRAY_CONCRETE, Blocks.CYAN_CONCRETE,
                Blocks.PURPLE_CONCRETE, Blocks.BLUE_CONCRETE, Blocks.BROWN_CONCRETE, Blocks.GREEN_CONCRETE, Blocks.RED_CONCRETE,
                Blocks.BLACK_CONCRETE, Blocks.DEAD_TUBE_CORAL_BLOCK, Blocks.DEAD_BRAIN_CORAL_BLOCK, Blocks.DEAD_BUBBLE_CORAL_BLOCK, Blocks.DEAD_FIRE_CORAL_BLOCK,
                Blocks.DEAD_HORN_CORAL_BLOCK, Blocks.TUBE_CORAL_BLOCK, Blocks.BRAIN_CORAL_BLOCK, Blocks.BUBBLE_CORAL_BLOCK, Blocks.FIRE_CORAL_BLOCK,
                Blocks.HORN_CORAL_BLOCK, Blocks.DEAD_TUBE_CORAL, Blocks.DEAD_BRAIN_CORAL, Blocks.DEAD_BUBBLE_CORAL, Blocks.DEAD_FIRE_CORAL,
                Blocks.DEAD_HORN_CORAL, Blocks.DEAD_TUBE_CORAL_FAN, Blocks.DEAD_BRAIN_CORAL_FAN, Blocks.DEAD_BUBBLE_CORAL_FAN, Blocks.DEAD_FIRE_CORAL_FAN,
                Blocks.DEAD_HORN_CORAL_FAN, Blocks.DEAD_TUBE_CORAL_WALL_FAN, Blocks.DEAD_BRAIN_CORAL_WALL_FAN, Blocks.DEAD_BUBBLE_CORAL_WALL_FAN, Blocks.DEAD_FIRE_CORAL_WALL_FAN,
                Blocks.DEAD_HORN_CORAL_WALL_FAN, Blocks.POLISHED_GRANITE_STAIRS, Blocks.SMOOTH_RED_SANDSTONE_STAIRS, Blocks.MOSSY_STONE_BRICK_STAIRS, Blocks.POLISHED_DIORITE_STAIRS,
                Blocks.MOSSY_COBBLESTONE_STAIRS, Blocks.END_STONE_BRICK_STAIRS, Blocks.STONE_STAIRS, Blocks.SMOOTH_SANDSTONE_STAIRS, Blocks.SMOOTH_QUARTZ_STAIRS,
                Blocks.GRANITE_STAIRS, Blocks.ANDESITE_STAIRS, Blocks.RED_NETHER_BRICK_STAIRS, Blocks.POLISHED_ANDESITE_STAIRS, Blocks.DIORITE_STAIRS,
                Blocks.POLISHED_GRANITE_SLAB, Blocks.SMOOTH_RED_SANDSTONE_SLAB, Blocks.MOSSY_STONE_BRICK_SLAB, Blocks.POLISHED_DIORITE_SLAB, Blocks.MOSSY_COBBLESTONE_SLAB,
                Blocks.END_STONE_BRICK_SLAB, Blocks.SMOOTH_SANDSTONE_SLAB, Blocks.SMOOTH_QUARTZ_SLAB, Blocks.GRANITE_SLAB, Blocks.ANDESITE_SLAB,
                Blocks.RED_NETHER_BRICK_SLAB, Blocks.POLISHED_ANDESITE_SLAB, Blocks.DIORITE_SLAB, Blocks.SMOKER, Blocks.BLAST_FURNACE,
                Blocks.GRINDSTONE, Blocks.STONECUTTER, Blocks.BELL, Blocks.LANTERN, Blocks.SOUL_LANTERN,
                Blocks.WARPED_NYLIUM, Blocks.CRIMSON_NYLIUM, Blocks.NETHERITE_BLOCK, Blocks.ANCIENT_DEBRIS, Blocks.CRYING_OBSIDIAN,
                Blocks.RESPAWN_ANCHOR, Blocks.LODESTONE, Blocks.BLACKSTONE, Blocks.BLACKSTONE_STAIRS, Blocks.BLACKSTONE_SLAB,
                Blocks.POLISHED_BLACKSTONE, Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS, Blocks.CHISELED_POLISHED_BLACKSTONE, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB,
                Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Blocks.GILDED_BLACKSTONE, Blocks.POLISHED_BLACKSTONE_STAIRS, Blocks.POLISHED_BLACKSTONE_SLAB, Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE,
                Blocks.CHISELED_NETHER_BRICKS, Blocks.CRACKED_NETHER_BRICKS, Blocks.QUARTZ_BRICKS, Blocks.ICE, Blocks.PACKED_ICE,
                Blocks.BLUE_ICE, Blocks.STONE_BUTTON, Blocks.PISTON, Blocks.STICKY_PISTON, Blocks.PISTON_HEAD,
                Blocks.INFESTED_COBBLESTONE, Blocks.INFESTED_CHISELED_STONE_BRICKS, Blocks.INFESTED_CRACKED_STONE_BRICKS, Blocks.INFESTED_DEEPSLATE, Blocks.INFESTED_STONE,
                Blocks.INFESTED_MOSSY_STONE_BRICKS, Blocks.INFESTED_STONE_BRICKS);
        tags.addTag(BlockTags.PICKAXE_MINEABLE, BlockTags.WALLS);
        tags.addTag(BlockTags.PICKAXE_MINEABLE, BlockTags.SHULKER_BOXES);
        tags.addTag(BlockTags.PICKAXE_MINEABLE, BlockTags.ANVIL);
        tags.addTag(BlockTags.PICKAXE_MINEABLE, BlockTags.CAULDRONS);
        tags.addTag(BlockTags.PICKAXE_MINEABLE, BlockTags.RAILS);
        tags.add(BlockTags.SHOVEL_MINEABLE,
                Blocks.CLAY, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL, Blocks.FARMLAND,
                Blocks.GRASS_BLOCK, Blocks.GRAVEL, Blocks.MYCELIUM, Blocks.SAND, Blocks.RED_SAND,
                Blocks.SNOW_BLOCK, Blocks.SNOW, Blocks.SOUL_SAND, Blocks.DIRT_PATH, Blocks.WHITE_CONCRETE_POWDER,
                Blocks.ORANGE_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE_POWDER, Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE_POWDER, Blocks.LIME_CONCRETE_POWDER,
                Blocks.PINK_CONCRETE_POWDER, Blocks.GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CONCRETE_POWDER, Blocks.CYAN_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE_POWDER,
                Blocks.BLUE_CONCRETE_POWDER, Blocks.BROWN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE_POWDER, Blocks.RED_CONCRETE_POWDER, Blocks.BLACK_CONCRETE_POWDER,
                Blocks.SOUL_SOIL);
        tags.add(BlockTags.NEEDS_STONE_TOOL, Blocks.IRON_BLOCK, Blocks.IRON_ORE, Blocks.LAPIS_BLOCK, Blocks.LAPIS_ORE);
        tags.add(BlockTags.NEEDS_IRON_TOOL, Blocks.DIAMOND_BLOCK, Blocks.DIAMOND_ORE, Blocks.EMERALD_ORE, Blocks.EMERALD_BLOCK, Blocks.GOLD_BLOCK, Blocks.GOLD_ORE, Blocks.REDSTONE_ORE);
        tags.add(BlockTags.NEEDS_DIAMOND_TOOL, Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.NETHERITE_BLOCK, Blocks.RESPAWN_ANCHOR, Blocks.ANCIENT_DEBRIS);
        tags.add(BlockTags.FEATURES_CANNOT_REPLACE, Blocks.BEDROCK, Blocks.SPAWNER, Blocks.CHEST, Blocks.END_PORTAL_FRAME);
        tags.addTag(BlockTags.LAVA_POOL_STONE_CANNOT_REPLACE, BlockTags.FEATURES_CANNOT_REPLACE);
        tags.addTag(BlockTags.LAVA_POOL_STONE_CANNOT_REPLACE, BlockTags.LEAVES);
        tags.add(BlockTags.GEODE_INVALID_BLOCKS, Blocks.BEDROCK, Blocks.WATER, Blocks.LAVA, Blocks.ICE, Blocks.PACKED_ICE, Blocks.BLUE_ICE);
        super.addExtraBlockTags(tags);
    }

    @Override
    public void addExtraItemTags(TagRegistry<Item> tags, TagRegistry<Block> blockTags) {
        tags.add(ItemTags.IGNORED_BY_PIGLIN_BABIES, Items.LEATHER);
        tags.add(ItemTags.PIGLIN_FOOD, Items.PORKCHOP, Items.COOKED_PORKCHOP);
        copyBlocks(tags, blockTags, ItemTags.CANDLES, BlockTags.CANDLES);
        tags.add(ItemTags.FREEZE_IMMUNE_WEARABLES, Items.LEATHER_BOOTS, Items.LEATHER_LEGGINGS, Items.LEATHER_CHESTPLATE, Items.LEATHER_HELMET, Items.LEATHER_HORSE_ARMOR);
        tags.add(ItemTags.AXOLOTL_TEMPT_ITEMS, Items.TROPICAL_FISH, Items.TROPICAL_FISH_BUCKET);
        copyBlocks(tags, blockTags, ItemTags.OCCLUDES_VIBRATION_SIGNALS, BlockTags.OCCLUDES_VIBRATION_SIGNALS);
        tags.add(ItemTags.FOX_FOOD, Items.SWEET_BERRIES);
        copyBlocks(tags, blockTags, ItemTags.IRON_ORES, BlockTags.IRON_ORES);
        copyBlocks(tags, blockTags, ItemTags.DIAMOND_ORES, BlockTags.DIAMOND_ORES);
        copyBlocks(tags, blockTags, ItemTags.REDSTONE_ORES, BlockTags.REDSTONE_ORES);
        copyBlocks(tags, blockTags, ItemTags.LAPIS_ORES, BlockTags.LAPIS_ORES);
        copyBlocks(tags, blockTags, ItemTags.COAL_ORES, BlockTags.COAL_ORES);
        copyBlocks(tags, blockTags, ItemTags.EMERALD_ORES, BlockTags.EMERALD_ORES);
        copyBlocks(tags, blockTags, ItemTags.COPPER_ORES, BlockTags.COPPER_ORES);
        tags.add(ItemTags.CLUSTER_MAX_HARVESTABLES, Items.DIAMOND_PICKAXE, Items.GOLDEN_PICKAXE, Items.IRON_PICKAXE, Items.NETHERITE_PICKAXE, Items.STONE_PICKAXE, Items.WOODEN_PICKAXE);
        super.addExtraItemTags(tags, blockTags);
    }

    @Override
    public void addExtraEntityTags(TagRegistry<EntityType<?>> tags) {
        tags.add(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS, EntityType.RABBIT, EntityType.ENDERMITE, EntityType.SILVERFISH);
        tags.add(EntityTypeTags.AXOLOTL_HUNT_TARGETS, EntityType.TROPICAL_FISH, EntityType.PUFFERFISH, EntityType.SALMON, EntityType.COD, EntityType.SQUID, EntityType.GLOW_SQUID);
        tags.add(EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES, EntityType.DROWNED, EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN);
        tags.add(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES, EntityType.STRIDER, EntityType.BLAZE, EntityType.MAGMA_CUBE);
        tags.add(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES, EntityType.STRAY, EntityType.POLAR_BEAR, EntityType.SNOW_GOLEM, EntityType.WITHER);
        super.addExtraEntityTags(tags);
    }

    @Override
    public void addExtraGameEventTags(TagRegistry<GameEvent> tags) {
        tags.add(GameEventTags.VIBRATIONS);
        tags.add(GameEventTags.IGNORE_VIBRATIONS_SNEAKING);
        super.addExtraGameEventTags(tags);
    }

    @Override
    public void preAcceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == ShulkerEntity.class && data == ShulkerEntityAccessor.getPeekAmount()) {
            DataTrackerManager.registerOldTrackedData(ShulkerEntity.class, OLD_SHULKER_ATTACHED_POSITION, Optional.empty(), (entity, pos) -> {});
        }
        super.preAcceptEntityData(clazz, data);
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == Entity.class && data == EntityAccessor.getFrozenTicks()) {
            return false;
        }
        return super.acceptEntityData(clazz, data);
    }
}
