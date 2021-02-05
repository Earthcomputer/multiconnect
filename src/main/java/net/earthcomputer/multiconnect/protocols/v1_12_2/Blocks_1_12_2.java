package net.earthcomputer.multiconnect.protocols.v1_12_2;

import net.earthcomputer.multiconnect.protocols.generic.ISimpleRegistry;
import net.minecraft.block.*;
import net.minecraft.block.enums.SlabType;
import net.minecraft.datafixer.fix.BlockStateFlattening;
import net.minecraft.datafixer.fix.EntityTheRenameningBlock;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

import static net.minecraft.block.Blocks.*;

public class Blocks_1_12_2 {

    private static SimpleRegistry<Block> REGISTRY_1_13;

    public static final Block FLOWING_WATER = new DummyBlock(WATER);
    public static final Block FLOWING_LAVA = new DummyBlock(LAVA);
    public static final Block DOUBLE_STONE_SLAB = new DummyBlock(SMOOTH_STONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE));
    public static final Block LIT_FURNACE = new DummyBlock(FURNACE.getDefaultState().with(FurnaceBlock.LIT, true));
    public static final Block LIT_REDSTONE_ORE = new DummyBlock(REDSTONE_ORE.getDefaultState().with(RedstoneOreBlock.LIT, true));
    public static final Block UNLIT_REDSTONE_TORCH = new DummyBlock(REDSTONE_TORCH.getDefaultState().with(RedstoneTorchBlock.LIT, false));
    public static final Block POWERED_REPEATER = new DummyBlock(REPEATER.getDefaultState().with(RepeaterBlock.POWERED, true));
    public static final Block LIT_REDSTONE_LAMP = new DummyBlock(REDSTONE_LAMP.getDefaultState().with(RedstoneLampBlock.LIT, true));
    public static final Block DOUBLE_WOODEN_SLAB = new DummyBlock(OAK_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE));
    public static final Block POWERED_COMPARATOR = new DummyBlock(COMPARATOR.getDefaultState().with(ComparatorBlock.POWERED, true));
    public static final Block DAYLIGHT_DETECTOR_INVERTED = new DummyBlock(DAYLIGHT_DETECTOR.getDefaultState().with(DaylightDetectorBlock.INVERTED, true));
    public static final Block DOUBLE_STONE_SLAB2 = new DummyBlock(RED_SANDSTONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE));
    public static final Block PURPUR_DOUBLE_SLAB = new DummyBlock(PURPUR_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE));

    // :thonkjang: made block states have 2 different IDs in 1.12
    // we use the one used in chunks as our actual IDs for efficiency,
    // but we have to convert the rest

    public static int convertToStateRegistryId(int id) {
        int blockId = id & 4095;
        int meta = id >> 12 & 15;
        return blockId << 4 | meta;
    }

    public static int convertFromStateRegistryId(int id) {
        int blockId = id >> 4 & 4095;
        int meta = id & 15;
        return meta << 12 | blockId;
    }

    private static void register(ISimpleRegistry<Block> registry, Block block, int id, String name) {
        RegistryKey<Block> key = RegistryKey.of(registry.getRegistryKey(), new Identifier(name));
        registry.registerInPlace(block, id, key, false);
    }

    private static void registerAliases(ISimpleRegistry<Block> registry) {
        final int torchId = Registry.BLOCK.getRawId(TORCH);
        final int flowerPotId = Registry.BLOCK.getRawId(FLOWER_POT);
        final int skullId = Registry.BLOCK.getRawId(SKELETON_SKULL);
        final int bedId = Registry.BLOCK.getRawId(WHITE_BED);
        final int bannerId = Registry.BLOCK.getRawId(BLACK_BANNER);
        final int wallBannerId = Registry.BLOCK.getRawId(BLACK_WALL_BANNER);

        for (int blockId = 1; blockId < 256; blockId++) {
            if (blockId == torchId) {
                register(registry, WALL_TORCH, 1 << 16 | blockId, "wall_torch");
            } else if (blockId == flowerPotId) {
                register(registry, POTTED_OAK_SAPLING, 1 << 16 | blockId, "potted_oak_sapling");
                register(registry, POTTED_SPRUCE_SAPLING, 2 << 16 | blockId, "potted_spruce_sapling");
                register(registry, POTTED_BIRCH_SAPLING, 3 << 16 | blockId, "potted_birch_sapling");
                register(registry, POTTED_JUNGLE_SAPLING, 4 << 16 | blockId, "potted_jungle_sapling");
                register(registry, POTTED_ACACIA_SAPLING, 5 << 16 | blockId, "potted_acacia_sapling");
                register(registry, POTTED_DARK_OAK_SAPLING, 6 << 16 | blockId, "potted_dark_oak_sapling");
                register(registry, POTTED_FERN, 7 << 16 | blockId, "potted_fern");
                register(registry, POTTED_DANDELION, 8 << 16 | blockId, "potted_dandelion");
                register(registry, POTTED_POPPY, 9 << 16 | blockId, "potted_poppy");
                register(registry, POTTED_BLUE_ORCHID, 10 << 16 | blockId, "potted_blue_orchid");
                register(registry, POTTED_ALLIUM, 11 << 16 | blockId, "potted_allium");
                register(registry, POTTED_AZURE_BLUET, 12 << 16 | blockId, "potted_azure_bluet");
                register(registry, POTTED_RED_TULIP, 13 << 16 | blockId, "potted_red_tulip");
                register(registry, POTTED_ORANGE_TULIP, 14 << 16 | blockId, "potted_orange_tulip");
                register(registry, POTTED_WHITE_TULIP, 15 << 16 | blockId, "potted_white_tulip");
                register(registry, POTTED_PINK_TULIP, 16 << 16 | flowerPotId, "potted_pink_tulip");
                register(registry, POTTED_OXEYE_DAISY, 17 << 16 | flowerPotId, "potted_oxeye_daisy");
                register(registry, POTTED_RED_MUSHROOM, 18 << 16 | flowerPotId, "potted_red_mushroom");
                register(registry, POTTED_BROWN_MUSHROOM, 19 << 16 | flowerPotId, "potted_brown_mushroom");
                register(registry, POTTED_DEAD_BUSH, 20 << 16 | flowerPotId, "potted_dead_bush");
                register(registry, POTTED_CACTUS, 21 << 16 | flowerPotId, "potted_cactus");
            } else if (blockId == skullId) {
                register(registry, SKELETON_WALL_SKULL, 1 << 16 | blockId, "skeleton_wall_skull");
                register(registry, WITHER_SKELETON_SKULL, 2 << 16 | blockId, "wither_skeleton_skull");
                register(registry, WITHER_SKELETON_WALL_SKULL, 3 << 16 | blockId, "wither_skeleton_wall_skull");
                register(registry, ZOMBIE_HEAD, 4 << 16 | blockId, "zombie_head");
                register(registry, ZOMBIE_WALL_HEAD, 5 << 16 | blockId, "zombie_wall_head");
                register(registry, PLAYER_HEAD, 6 << 16 | blockId, "player_head");
                register(registry, PLAYER_WALL_HEAD, 7 << 16 | blockId, "player_wall_head");
                register(registry, CREEPER_HEAD, 8 << 16 | blockId, "creeper_head");
                register(registry, CREEPER_WALL_HEAD, 9 << 16 | blockId, "creeper_wall_head");
                register(registry, DRAGON_HEAD, 10 << 16 | blockId, "dragon_head");
                register(registry, DRAGON_WALL_HEAD, 11 << 16 | blockId, "dragon_wall_head");
            } else if (blockId == bedId) {
                register(registry, ORANGE_BED, 1 << 16 | blockId, "orange_bed");
                register(registry, MAGENTA_BED, 2 << 16 | blockId, "magenta_bed");
                register(registry, LIGHT_BLUE_BED, 3 << 16 | blockId, "light_blue_bed");
                register(registry, YELLOW_BED, 4 << 16 | blockId, "yellow_bed");
                register(registry, LIME_BED, 5 << 16 | blockId, "lime_bed");
                register(registry, PINK_BED, 6 << 16 | blockId, "pink_bed");
                register(registry, GRAY_BED, 7 << 16 | blockId, "gray_bed");
                register(registry, LIGHT_GRAY_BED, 8 << 16 | blockId, "light_gray_bed");
                register(registry, CYAN_BED, 9 << 16 | blockId, "cyan_bed");
                register(registry, PURPLE_BED, 10 << 16 | blockId, "purple_bed");
                register(registry, BLUE_BED, 11 << 16 | blockId, "blue_bed");
                register(registry, BROWN_BED, 12 << 16 | blockId, "brown_bed");
                register(registry, GREEN_BED, 13 << 16 | blockId, "green_bed");
                register(registry, RED_BED, 14 << 16 | blockId, "red_bed");
                register(registry, BLACK_BED, 15 << 16 | blockId, "black_bed");
            } else if (blockId == bannerId) {
                register(registry, ORANGE_BANNER, 1 << 16 | blockId, "orange_banner");
                register(registry, MAGENTA_BANNER, 2 << 16 | blockId, "magenta_banner");
                register(registry, LIGHT_BLUE_BANNER, 3 << 16 | blockId, "light_blue_banner");
                register(registry, YELLOW_BANNER, 4 << 16 | blockId, "yellow_banner");
                register(registry, LIME_BANNER, 5 << 16 | blockId, "lime_banner");
                register(registry, PINK_BANNER, 6 << 16 | blockId, "pink_banner");
                register(registry, GRAY_BANNER, 7 << 16 | blockId, "gray_banner");
                register(registry, LIGHT_GRAY_BANNER, 8 << 16 | blockId, "light_gray_banner");
                register(registry, CYAN_BANNER, 9 << 16 | blockId, "cyan_banner");
                register(registry, PURPLE_BANNER, 10 << 16 | blockId, "purple_banner");
                register(registry, BLUE_BANNER, 11 << 16 | blockId, "blue_banner");
                register(registry, BROWN_BANNER, 12 << 16 | blockId, "brown_banner");
                register(registry, GREEN_BANNER, 13 << 16 | blockId, "green_banner");
                register(registry, RED_BANNER, 14 << 16 | blockId, "red_banner");
                register(registry, WHITE_BANNER, 15 << 16 | blockId, "black_banner");
            } else if (blockId == wallBannerId) {
                register(registry, ORANGE_WALL_BANNER, 1 << 16 | blockId, "orange_wall_banner");
                register(registry, MAGENTA_WALL_BANNER, 2 << 16 | blockId, "magenta_wall_banner");
                register(registry, LIGHT_BLUE_WALL_BANNER, 3 << 16 | blockId, "light_blue_wall_banner");
                register(registry, YELLOW_WALL_BANNER, 4 << 16 | blockId, "yellow_wall_banner");
                register(registry, LIME_WALL_BANNER, 5 << 16 | blockId, "lime_wall_banner");
                register(registry, PINK_WALL_BANNER, 6 << 16 | blockId, "pink_wall_banner");
                register(registry, GRAY_WALL_BANNER, 7 << 16 | blockId, "gray_wall_banner");
                register(registry, LIGHT_GRAY_WALL_BANNER, 8 << 16 | blockId, "light_gray_wall_banner");
                register(registry, CYAN_WALL_BANNER, 9 << 16 | blockId, "cyan_wall_banner");
                register(registry, PURPLE_WALL_BANNER, 10 << 16 | blockId, "purple_wall_banner");
                register(registry, BLUE_WALL_BANNER, 11 << 16 | blockId, "blue_wall_banner");
                register(registry, BROWN_WALL_BANNER, 12 << 16 | blockId, "brown_wall_banner");
                register(registry, GREEN_WALL_BANNER, 13 << 16 | blockId, "green_wall_banner");
                register(registry, RED_WALL_BANNER, 14 << 16 | blockId, "red_wall_banner");
                register(registry, WHITE_WALL_BANNER, 15 << 16 | blockId, "black_wall_banner");
            } else {
                for (int meta = 1; meta < 16; meta++) {
                    String fixedName = BlockStateFlattening.lookupStateBlock(blockId << 4 | meta);
                    fixedName = EntityTheRenameningBlock.BLOCKS.getOrDefault(fixedName, fixedName);
                    if ("minecraft:melon_block".equals(fixedName)) fixedName = "minecraft:melon";
                    Identifier subName = new Identifier(fixedName);
                    Block subBlock = REGISTRY_1_13.get(subName);
                    if (subBlock != AIR && Registry.BLOCK.getRawId(subBlock) == 0) {
                        while (Registry.BLOCK.get(subName) != Blocks.AIR)
                            subName = new Identifier(subName.getNamespace(), subName.getPath() + "_");
                        RegistryKey<Block> key = RegistryKey.of(registry.getRegistryKey(), subName);
                        registry.registerInPlace(subBlock, meta << 16 | blockId, key, false);
                    }
                }
            }
        }


    }

    private static void fixBlockItems() {
        Item.BLOCK_ITEMS.keySet().removeIf(block -> block != AIR && Registry.BLOCK.getRawId(block) == 0);
    }
    
    public static void registerBlocks(ISimpleRegistry<Block> registry) {
        if (REGISTRY_1_13 == null)
            REGISTRY_1_13 = registry.copy();

        registry.clear(false);

        register(registry, AIR, 0, "air");
        register(registry, STONE, 1, "stone");
        register(registry, GRASS_BLOCK, 2, "grass");
        register(registry, DIRT, 3, "dirt");
        register(registry, COBBLESTONE, 4, "cobblestone");
        register(registry, OAK_PLANKS, 5, "planks");
        register(registry, OAK_SAPLING, 6, "sapling");
        register(registry, BEDROCK, 7, "bedrock");
        register(registry, FLOWING_WATER, 8, "flowing_water");
        register(registry, WATER, 9, "water");
        register(registry, FLOWING_LAVA, 10, "flowing_lava");
        register(registry, LAVA, 11, "lava");
        register(registry, SAND, 12, "sand");
        register(registry, GRAVEL, 13, "gravel");
        register(registry, GOLD_ORE, 14, "gold_ore");
        register(registry, IRON_ORE, 15, "iron_ore");
        register(registry, COAL_ORE, 16, "coal_ore");
        register(registry, OAK_LOG, 17, "log");
        register(registry, OAK_LEAVES, 18, "leaves");
        register(registry, SPONGE, 19, "sponge");
        register(registry, GLASS, 20, "glass");
        register(registry, LAPIS_ORE, 21, "lapis_ore");
        register(registry, LAPIS_BLOCK, 22, "lapis_block");
        register(registry, DISPENSER, 23, "dispenser");
        register(registry, SANDSTONE, 24, "sandstone");
        register(registry, NOTE_BLOCK, 25, "noteblock");
        register(registry, WHITE_BED, 26, "bed");
        register(registry, POWERED_RAIL, 27, "golden_rail");
        register(registry, DETECTOR_RAIL, 28, "detector_rail");
        register(registry, STICKY_PISTON, 29, "sticky_piston");
        register(registry, COBWEB, 30, "web");
        register(registry, GRASS, 31, "tallgrass");
        register(registry, DEAD_BUSH, 32, "deadbush");
        register(registry, PISTON, 33, "piston");
        register(registry, PISTON_HEAD, 34, "piston_head");
        register(registry, WHITE_WOOL, 35, "wool");
        register(registry, MOVING_PISTON, 36, "piston_extension");
        register(registry, DANDELION, 37, "yellow_flower");
        register(registry, POPPY, 38, "red_flower");
        register(registry, BROWN_MUSHROOM, 39, "brown_mushroom");
        register(registry, RED_MUSHROOM, 40, "red_mushroom");
        register(registry, GOLD_BLOCK, 41, "gold_block");
        register(registry, IRON_BLOCK, 42, "iron_block");
        register(registry, DOUBLE_STONE_SLAB, 43, "double_stone_slab");
        register(registry, SMOOTH_STONE_SLAB, 44, "stone_slab");
        register(registry, BRICKS, 45, "brick_block");
        register(registry, TNT, 46, "tnt");
        register(registry, BOOKSHELF, 47, "bookshelf");
        register(registry, MOSSY_COBBLESTONE, 48, "mossy_cobblestone");
        register(registry, OBSIDIAN, 49, "obsidian");
        register(registry, TORCH, 50, "torch");
        register(registry, FIRE, 51, "fire");
        register(registry, SPAWNER, 52, "mob_spawner");
        register(registry, OAK_STAIRS, 53, "oak_stairs");
        register(registry, CHEST, 54, "chest");
        register(registry, REDSTONE_WIRE, 55, "redstone_wire");
        register(registry, DIAMOND_ORE, 56, "diamond_ore");
        register(registry, DIAMOND_BLOCK, 57, "diamond_block");
        register(registry, CRAFTING_TABLE, 58, "crafting_table");
        register(registry, WHEAT, 59, "wheat");
        register(registry, FARMLAND, 60, "farmland");
        register(registry, FURNACE, 61, "furnace");
        register(registry, LIT_FURNACE, 62, "lit_furnace");
        register(registry, OAK_SIGN, 63, "standing_sign");
        register(registry, OAK_DOOR, 64, "wooden_door");
        register(registry, LADDER, 65, "ladder");
        register(registry, RAIL, 66, "rail");
        register(registry, COBBLESTONE_STAIRS, 67, "stone_stairs");
        register(registry, OAK_WALL_SIGN, 68, "wall_sign");
        register(registry, LEVER, 69, "lever");
        register(registry, STONE_PRESSURE_PLATE, 70, "stone_pressure_plate");
        register(registry, IRON_DOOR, 71, "iron_door");
        register(registry, OAK_PRESSURE_PLATE, 72, "wooden_pressure_plate");
        register(registry, REDSTONE_ORE, 73, "redstone_ore");
        register(registry, LIT_REDSTONE_ORE, 74, "lit_redstone_ore");
        register(registry, UNLIT_REDSTONE_TORCH, 75, "unlit_redstone_torch");
        register(registry, REDSTONE_TORCH, 76, "redstone_torch");
        register(registry, STONE_BUTTON, 77, "stone_button");
        register(registry, SNOW, 78, "snow_layer");
        register(registry, ICE, 79, "ice");
        register(registry, SNOW_BLOCK, 80, "snow");
        register(registry, CACTUS, 81, "cactus");
        register(registry, CLAY, 82, "clay");
        register(registry, SUGAR_CANE, 83, "reeds");
        register(registry, JUKEBOX, 84, "jukebox");
        register(registry, OAK_FENCE, 85, "fence");
        register(registry, CARVED_PUMPKIN, 86, "pumpkin");
        register(registry, NETHERRACK, 87, "netherrack");
        register(registry, SOUL_SAND, 88, "soul_sand");
        register(registry, GLOWSTONE, 89, "glowstone");
        register(registry, NETHER_PORTAL, 90, "portal");
        register(registry, JACK_O_LANTERN, 91, "lit_pumpkin");
        register(registry, CAKE, 92, "cake");
        register(registry, REPEATER, 93, "unpowered_repeater");
        register(registry, POWERED_REPEATER, 94, "powered_repeater");
        register(registry, WHITE_STAINED_GLASS, 95, "stained_glass");
        register(registry, OAK_TRAPDOOR, 96, "trapdoor");
        register(registry, INFESTED_STONE, 97, "monster_egg");
        register(registry, STONE_BRICKS, 98, "stonebrick");
        register(registry, BROWN_MUSHROOM_BLOCK, 99, "brown_mushroom_block");
        register(registry, RED_MUSHROOM_BLOCK, 100, "red_mushroom_block");
        register(registry, IRON_BARS, 101, "iron_bars");
        register(registry, GLASS_PANE, 102, "glass_pane");
        register(registry, MELON, 103, "melon_block");
        register(registry, PUMPKIN_STEM, 104, "pumpkin_stem");
        register(registry, MELON_STEM, 105, "melon_stem");
        register(registry, VINE, 106, "vine");
        register(registry, OAK_FENCE_GATE, 107, "fence_gate");
        register(registry, BRICK_STAIRS, 108, "brick_stairs");
        register(registry, STONE_BRICK_STAIRS, 109, "stone_brick_stairs");
        register(registry, MYCELIUM, 110, "mycelium");
        register(registry, LILY_PAD, 111, "waterlily");
        register(registry, NETHER_BRICKS, 112, "nether_brick");
        register(registry, NETHER_BRICK_FENCE, 113, "nether_brick_fence");
        register(registry, NETHER_BRICK_STAIRS, 114, "nether_brick_stairs");
        register(registry, NETHER_WART, 115, "nether_wart");
        register(registry, ENCHANTING_TABLE, 116, "enchanting_table");
        register(registry, BREWING_STAND, 117, "brewing_stand");
        register(registry, CAULDRON, 118, "cauldron");
        register(registry, END_PORTAL, 119, "end_portal");
        register(registry, END_PORTAL_FRAME, 120, "end_portal_frame");
        register(registry, END_STONE, 121, "end_stone");
        register(registry, DRAGON_EGG, 122, "dragon_egg");
        register(registry, REDSTONE_LAMP, 123, "redstone_lamp");
        register(registry, LIT_REDSTONE_LAMP, 124, "lit_redstone_lamp");
        register(registry, DOUBLE_WOODEN_SLAB, 125, "double_wooden_slab");
        register(registry, OAK_SLAB, 126, "wooden_slab");
        register(registry, COCOA, 127, "cocoa");
        register(registry, SANDSTONE_STAIRS, 128, "sandstone_stairs");
        register(registry, EMERALD_ORE, 129, "emerald_ore");
        register(registry, ENDER_CHEST, 130, "ender_chest");
        register(registry, TRIPWIRE_HOOK, 131, "tripwire_hook");
        register(registry, TRIPWIRE, 132, "tripwire");
        register(registry, EMERALD_BLOCK, 133, "emerald_block");
        register(registry, SPRUCE_STAIRS, 134, "spruce_stairs");
        register(registry, BIRCH_STAIRS, 135, "birch_stairs");
        register(registry, JUNGLE_STAIRS, 136, "jungle_stairs");
        register(registry, COMMAND_BLOCK, 137, "command_block");
        register(registry, BEACON, 138, "beacon");
        register(registry, COBBLESTONE_WALL, 139, "cobblestone_wall");
        register(registry, FLOWER_POT, 140, "flower_pot");
        register(registry, CARROTS, 141, "carrots");
        register(registry, POTATOES, 142, "potatoes");
        register(registry, OAK_BUTTON, 143, "wooden_button");
        register(registry, SKELETON_SKULL, 144, "skull");
        register(registry, ANVIL, 145, "anvil");
        register(registry, TRAPPED_CHEST, 146, "trapped_chest");
        register(registry, LIGHT_WEIGHTED_PRESSURE_PLATE, 147, "light_weighted_pressure_plate");
        register(registry, HEAVY_WEIGHTED_PRESSURE_PLATE, 148, "heavy_weighted_pressure_plate");
        register(registry, COMPARATOR, 149, "unpowered_comparator");
        register(registry, POWERED_COMPARATOR, 150, "powered_comparator");
        register(registry, DAYLIGHT_DETECTOR, 151, "daylight_detector");
        register(registry, REDSTONE_BLOCK, 152, "redstone_block");
        register(registry, NETHER_QUARTZ_ORE, 153, "quartz_ore");
        register(registry, HOPPER, 154, "hopper");
        register(registry, QUARTZ_BLOCK, 155, "quartz_block");
        register(registry, QUARTZ_STAIRS, 156, "quartz_stairs");
        register(registry, ACTIVATOR_RAIL, 157, "activator_rail");
        register(registry, DROPPER, 158, "dropper");
        register(registry, WHITE_TERRACOTTA, 159, "stained_hardened_clay");
        register(registry, WHITE_STAINED_GLASS_PANE, 160, "stained_glass_pane");
        register(registry, ACACIA_LEAVES, 161, "leaves2");
        register(registry, ACACIA_LOG, 162, "log2");
        register(registry, ACACIA_STAIRS, 163, "acacia_stairs");
        register(registry, DARK_OAK_STAIRS, 164, "dark_oak_stairs");
        register(registry, SLIME_BLOCK, 165, "slime");
        register(registry, BARRIER, 166, "barrier");
        register(registry, IRON_TRAPDOOR, 167, "iron_trapdoor");
        register(registry, PRISMARINE, 168, "prismarine");
        register(registry, SEA_LANTERN, 169, "sea_lantern");
        register(registry, HAY_BLOCK, 170, "hay_block");
        register(registry, WHITE_CARPET, 171, "carpet");
        register(registry, TERRACOTTA, 172, "hardened_clay");
        register(registry, COAL_BLOCK, 173, "coal_block");
        register(registry, PACKED_ICE, 174, "packed_ice");
        register(registry, SUNFLOWER, 175, "double_plant");
        register(registry, BLACK_BANNER, 176, "standing_banner");
        register(registry, BLACK_WALL_BANNER, 177, "wall_banner");
        register(registry, DAYLIGHT_DETECTOR_INVERTED, 178, "daylight_detector_inverted");
        register(registry, RED_SANDSTONE, 179, "red_sandstone");
        register(registry, RED_SANDSTONE_STAIRS, 180, "red_sandstone_stairs");
        register(registry, DOUBLE_STONE_SLAB2, 181, "double_stone_slab2");
        register(registry, RED_SANDSTONE_SLAB, 182, "stone_slab2");
        register(registry, SPRUCE_FENCE_GATE, 183, "spruce_fence_gate");
        register(registry, BIRCH_FENCE_GATE, 184, "birch_fence_gate");
        register(registry, JUNGLE_FENCE_GATE, 185, "jungle_fence_gate");
        register(registry, DARK_OAK_FENCE_GATE, 186, "dark_oak_fence_gate");
        register(registry, ACACIA_FENCE_GATE, 187, "acacia_fence_gate");
        register(registry, SPRUCE_FENCE, 188, "spruce_fence");
        register(registry, BIRCH_FENCE, 189, "birch_fence");
        register(registry, JUNGLE_FENCE, 190, "jungle_fence");
        register(registry, DARK_OAK_FENCE, 191, "dark_oak_fence");
        register(registry, ACACIA_FENCE, 192, "acacia_fence");
        register(registry, SPRUCE_DOOR, 193, "spruce_door");
        register(registry, BIRCH_DOOR, 194, "birch_door");
        register(registry, JUNGLE_DOOR, 195, "jungle_door");
        register(registry, ACACIA_DOOR, 196, "acacia_door");
        register(registry, DARK_OAK_DOOR, 197, "dark_oak_door");
        register(registry, END_ROD, 198, "end_rod");
        register(registry, CHORUS_PLANT, 199, "chorus_plant");
        register(registry, CHORUS_FLOWER, 200, "chorus_flower");
        register(registry, PURPUR_BLOCK, 201, "purpur_block");
        register(registry, PURPUR_PILLAR, 202, "purpur_pillar");
        register(registry, PURPUR_STAIRS, 203, "purpur_stairs");
        register(registry, PURPUR_DOUBLE_SLAB, 204, "purpur_double_slab");
        register(registry, PURPUR_SLAB, 205, "purpur_slab");
        register(registry, END_STONE_BRICKS, 206, "end_bricks");
        register(registry, BEETROOTS, 207, "beetroots");
        register(registry, DIRT_PATH, 208, "grass_path");
        register(registry, END_GATEWAY, 209, "end_gateway");
        register(registry, REPEATING_COMMAND_BLOCK, 210, "repeating_command_block");
        register(registry, CHAIN_COMMAND_BLOCK, 211, "chain_command_block");
        register(registry, FROSTED_ICE, 212, "frosted_ice");
        register(registry, MAGMA_BLOCK, 213, "magma");
        register(registry, NETHER_WART_BLOCK, 214, "nether_wart_block");
        register(registry, RED_NETHER_BRICKS, 215, "red_nether_brick");
        register(registry, BONE_BLOCK, 216, "bone_block");
        register(registry, STRUCTURE_VOID, 217, "structure_void");
        register(registry, OBSERVER, 218, "observer");
        register(registry, WHITE_SHULKER_BOX, 219, "white_shulker_box");
        register(registry, ORANGE_SHULKER_BOX, 220, "orange_shulker_box");
        register(registry, MAGENTA_SHULKER_BOX, 221, "magenta_shulker_box");
        register(registry, LIGHT_BLUE_SHULKER_BOX, 222, "light_blue_shulker_box");
        register(registry, YELLOW_SHULKER_BOX, 223, "yellow_shulker_box");
        register(registry, LIME_SHULKER_BOX, 224, "lime_shulker_box");
        register(registry, PINK_SHULKER_BOX, 225, "pink_shulker_box");
        register(registry, GRAY_SHULKER_BOX, 226, "gray_shulker_box");
        register(registry, LIGHT_GRAY_SHULKER_BOX, 227, "silver_shulker_box");
        register(registry, CYAN_SHULKER_BOX, 228, "cyan_shulker_box");
        register(registry, PURPLE_SHULKER_BOX, 229, "purple_shulker_box");
        register(registry, BLUE_SHULKER_BOX, 230, "blue_shulker_box");
        register(registry, BROWN_SHULKER_BOX, 231, "brown_shulker_box");
        register(registry, GREEN_SHULKER_BOX, 232, "green_shulker_box");
        register(registry, RED_SHULKER_BOX, 233, "red_shulker_box");
        register(registry, BLACK_SHULKER_BOX, 234, "black_shulker_box");
        register(registry, WHITE_GLAZED_TERRACOTTA, 235, "white_glazed_terracotta");
        register(registry, ORANGE_GLAZED_TERRACOTTA, 236, "orange_glazed_terracotta");
        register(registry, MAGENTA_GLAZED_TERRACOTTA, 237, "magenta_glazed_terracotta");
        register(registry, LIGHT_BLUE_GLAZED_TERRACOTTA, 238, "light_blue_glazed_terracotta");
        register(registry, YELLOW_GLAZED_TERRACOTTA, 239, "yellow_glazed_terracotta");
        register(registry, LIME_GLAZED_TERRACOTTA, 240, "lime_glazed_terracotta");
        register(registry, PINK_GLAZED_TERRACOTTA, 241, "pink_glazed_terracotta");
        register(registry, GRAY_GLAZED_TERRACOTTA, 242, "gray_glazed_terracotta");
        register(registry, LIGHT_GRAY_GLAZED_TERRACOTTA, 243, "silver_glazed_terracotta");
        register(registry, CYAN_GLAZED_TERRACOTTA, 244, "cyan_glazed_terracotta");
        register(registry, PURPLE_GLAZED_TERRACOTTA, 245, "purple_glazed_terracotta");
        register(registry, BLUE_GLAZED_TERRACOTTA, 246, "blue_glazed_terracotta");
        register(registry, BROWN_GLAZED_TERRACOTTA, 247, "brown_glazed_terracotta");
        register(registry, GREEN_GLAZED_TERRACOTTA, 248, "green_glazed_terracotta");
        register(registry, RED_GLAZED_TERRACOTTA, 249, "red_glazed_terracotta");
        register(registry, BLACK_GLAZED_TERRACOTTA, 250, "black_glazed_terracotta");
        register(registry, WHITE_CONCRETE, 251, "concrete");
        register(registry, WHITE_CONCRETE_POWDER, 252, "concrete_powder");
        register(registry, STRUCTURE_BLOCK, 255, "structure_block");

        registerAliases(registry);
        fixBlockItems();
    }

}
