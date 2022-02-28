package net.earthcomputer.multiconnect.protocols.v1_12_2;

import net.earthcomputer.multiconnect.protocols.generic.RegistryBuilder;
import net.minecraft.block.*;
import net.minecraft.block.enums.SlabType;
import net.minecraft.datafixer.fix.BlockStateFlattening;
import net.minecraft.datafixer.fix.EntityTheRenameningBlock;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
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

    private static void registerAliases(RegistryBuilder<Block> registry) {
        final int torchId = Registry.BLOCK.getRawId(TORCH);
        final int flowerPotId = Registry.BLOCK.getRawId(FLOWER_POT);
        final int skullId = Registry.BLOCK.getRawId(SKELETON_SKULL);
        final int bedId = Registry.BLOCK.getRawId(WHITE_BED);
        final int bannerId = Registry.BLOCK.getRawId(BLACK_BANNER);
        final int wallBannerId = Registry.BLOCK.getRawId(BLACK_WALL_BANNER);

        for (int blockId = 1; blockId < 256; blockId++) {
            if (blockId == torchId) {
                registry.registerInPlace(1 << 16 | blockId, WALL_TORCH, "wall_torch");
            } else if (blockId == flowerPotId) {
                registry.registerInPlace(1 << 16 | blockId, POTTED_OAK_SAPLING, "potted_oak_sapling");
                registry.registerInPlace(2 << 16 | blockId, POTTED_SPRUCE_SAPLING, "potted_spruce_sapling");
                registry.registerInPlace(3 << 16 | blockId, POTTED_BIRCH_SAPLING, "potted_birch_sapling");
                registry.registerInPlace(4 << 16 | blockId, POTTED_JUNGLE_SAPLING, "potted_jungle_sapling");
                registry.registerInPlace(5 << 16 | blockId, POTTED_ACACIA_SAPLING, "potted_acacia_sapling");
                registry.registerInPlace(6 << 16 | blockId, POTTED_DARK_OAK_SAPLING, "potted_dark_oak_sapling");
                registry.registerInPlace(7 << 16 | blockId, POTTED_FERN, "potted_fern");
                registry.registerInPlace(8 << 16 | blockId, POTTED_DANDELION, "potted_dandelion");
                registry.registerInPlace(9 << 16 | blockId, POTTED_POPPY, "potted_poppy");
                registry.registerInPlace(10 << 16 | blockId, POTTED_BLUE_ORCHID, "potted_blue_orchid");
                registry.registerInPlace(11 << 16 | blockId, POTTED_ALLIUM, "potted_allium");
                registry.registerInPlace(12 << 16 | blockId, POTTED_AZURE_BLUET, "potted_azure_bluet");
                registry.registerInPlace(13 << 16 | blockId, POTTED_RED_TULIP, "potted_red_tulip");
                registry.registerInPlace(14 << 16 | blockId, POTTED_ORANGE_TULIP, "potted_orange_tulip");
                registry.registerInPlace(15 << 16 | blockId, POTTED_WHITE_TULIP, "potted_white_tulip");
                registry.registerInPlace(16 << 16 | flowerPotId, POTTED_PINK_TULIP, "potted_pink_tulip");
                registry.registerInPlace(17 << 16 | flowerPotId, POTTED_OXEYE_DAISY, "potted_oxeye_daisy");
                registry.registerInPlace(18 << 16 | flowerPotId, POTTED_RED_MUSHROOM, "potted_red_mushroom");
                registry.registerInPlace(19 << 16 | flowerPotId, POTTED_BROWN_MUSHROOM, "potted_brown_mushroom");
                registry.registerInPlace(20 << 16 | flowerPotId, POTTED_DEAD_BUSH, "potted_dead_bush");
                registry.registerInPlace(21 << 16 | flowerPotId, POTTED_CACTUS, "potted_cactus");
            } else if (blockId == skullId) {
                registry.registerInPlace(1 << 16 | blockId, SKELETON_WALL_SKULL, "skeleton_wall_skull");
                registry.registerInPlace(2 << 16 | blockId, WITHER_SKELETON_SKULL, "wither_skeleton_skull");
                registry.registerInPlace(3 << 16 | blockId, WITHER_SKELETON_WALL_SKULL, "wither_skeleton_wall_skull");
                registry.registerInPlace(4 << 16 | blockId, ZOMBIE_HEAD, "zombie_head");
                registry.registerInPlace(5 << 16 | blockId, ZOMBIE_WALL_HEAD, "zombie_wall_head");
                registry.registerInPlace(6 << 16 | blockId, PLAYER_HEAD, "player_head");
                registry.registerInPlace(7 << 16 | blockId, PLAYER_WALL_HEAD, "player_wall_head");
                registry.registerInPlace(8 << 16 | blockId, CREEPER_HEAD, "creeper_head");
                registry.registerInPlace(9 << 16 | blockId, CREEPER_WALL_HEAD, "creeper_wall_head");
                registry.registerInPlace(10 << 16 | blockId, DRAGON_HEAD, "dragon_head");
                registry.registerInPlace(11 << 16 | blockId, DRAGON_WALL_HEAD, "dragon_wall_head");
            } else if (blockId == bedId) {
                registry.registerInPlace(1 << 16 | blockId, ORANGE_BED, "orange_bed");
                registry.registerInPlace(2 << 16 | blockId, MAGENTA_BED, "magenta_bed");
                registry.registerInPlace(3 << 16 | blockId, LIGHT_BLUE_BED, "light_blue_bed");
                registry.registerInPlace(4 << 16 | blockId, YELLOW_BED, "yellow_bed");
                registry.registerInPlace(5 << 16 | blockId, LIME_BED, "lime_bed");
                registry.registerInPlace(6 << 16 | blockId, PINK_BED, "pink_bed");
                registry.registerInPlace(7 << 16 | blockId, GRAY_BED, "gray_bed");
                registry.registerInPlace(8 << 16 | blockId, LIGHT_GRAY_BED, "light_gray_bed");
                registry.registerInPlace(9 << 16 | blockId, CYAN_BED, "cyan_bed");
                registry.registerInPlace(10 << 16 | blockId, PURPLE_BED, "purple_bed");
                registry.registerInPlace(11 << 16 | blockId, BLUE_BED, "blue_bed");
                registry.registerInPlace(12 << 16 | blockId, BROWN_BED, "brown_bed");
                registry.registerInPlace(13 << 16 | blockId, GREEN_BED, "green_bed");
                registry.registerInPlace(14 << 16 | blockId, RED_BED, "red_bed");
                registry.registerInPlace(15 << 16 | blockId, BLACK_BED, "black_bed");
            } else if (blockId == bannerId) {
                registry.registerInPlace(1 << 16 | blockId, ORANGE_BANNER, "orange_banner");
                registry.registerInPlace(2 << 16 | blockId, MAGENTA_BANNER, "magenta_banner");
                registry.registerInPlace(3 << 16 | blockId, LIGHT_BLUE_BANNER, "light_blue_banner");
                registry.registerInPlace(4 << 16 | blockId, YELLOW_BANNER, "yellow_banner");
                registry.registerInPlace(5 << 16 | blockId, LIME_BANNER, "lime_banner");
                registry.registerInPlace(6 << 16 | blockId, PINK_BANNER, "pink_banner");
                registry.registerInPlace(7 << 16 | blockId, GRAY_BANNER, "gray_banner");
                registry.registerInPlace(8 << 16 | blockId, LIGHT_GRAY_BANNER, "light_gray_banner");
                registry.registerInPlace(9 << 16 | blockId, CYAN_BANNER, "cyan_banner");
                registry.registerInPlace(10 << 16 | blockId, PURPLE_BANNER, "purple_banner");
                registry.registerInPlace(11 << 16 | blockId, BLUE_BANNER, "blue_banner");
                registry.registerInPlace(12 << 16 | blockId, BROWN_BANNER, "brown_banner");
                registry.registerInPlace(13 << 16 | blockId, GREEN_BANNER, "green_banner");
                registry.registerInPlace(14 << 16 | blockId, RED_BANNER, "red_banner");
                registry.registerInPlace(15 << 16 | blockId, WHITE_BANNER, "black_banner");
            } else if (blockId == wallBannerId) {
                registry.registerInPlace(1 << 16 | blockId, ORANGE_WALL_BANNER, "orange_wall_banner");
                registry.registerInPlace(2 << 16 | blockId, MAGENTA_WALL_BANNER, "magenta_wall_banner");
                registry.registerInPlace(3 << 16 | blockId, LIGHT_BLUE_WALL_BANNER, "light_blue_wall_banner");
                registry.registerInPlace(4 << 16 | blockId, YELLOW_WALL_BANNER, "yellow_wall_banner");
                registry.registerInPlace(5 << 16 | blockId, LIME_WALL_BANNER, "lime_wall_banner");
                registry.registerInPlace(6 << 16 | blockId, PINK_WALL_BANNER, "pink_wall_banner");
                registry.registerInPlace(7 << 16 | blockId, GRAY_WALL_BANNER, "gray_wall_banner");
                registry.registerInPlace(8 << 16 | blockId, LIGHT_GRAY_WALL_BANNER, "light_gray_wall_banner");
                registry.registerInPlace(9 << 16 | blockId, CYAN_WALL_BANNER, "cyan_wall_banner");
                registry.registerInPlace(10 << 16 | blockId, PURPLE_WALL_BANNER, "purple_wall_banner");
                registry.registerInPlace(11 << 16 | blockId, BLUE_WALL_BANNER, "blue_wall_banner");
                registry.registerInPlace(12 << 16 | blockId, BROWN_WALL_BANNER, "brown_wall_banner");
                registry.registerInPlace(13 << 16 | blockId, GREEN_WALL_BANNER, "green_wall_banner");
                registry.registerInPlace(14 << 16 | blockId, RED_WALL_BANNER, "red_wall_banner");
                registry.registerInPlace(15 << 16 | blockId, WHITE_WALL_BANNER, "black_wall_banner");
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
                        registry.registerInPlace(meta << 16 | blockId, subBlock, subName);
                    }
                }
            }
        }
    }
    
    public static void registerBlocks(RegistryBuilder<Block> registry) {
        if (REGISTRY_1_13 == null)
            REGISTRY_1_13 = registry.createCopiedRegistry();

        registry.disableSideEffects();

        registry.clear();

        registry.registerInPlace(0, AIR, "air");
        registry.registerInPlace(1, STONE, "stone");
        registry.registerInPlace(2, GRASS_BLOCK, "grass");
        registry.registerInPlace(3, DIRT, "dirt");
        registry.registerInPlace(4, COBBLESTONE, "cobblestone");
        registry.registerInPlace(5, OAK_PLANKS, "planks");
        registry.registerInPlace(6, OAK_SAPLING, "sapling");
        registry.registerInPlace(7, BEDROCK, "bedrock");
        registry.registerInPlace(8, FLOWING_WATER, "flowing_water");
        registry.registerInPlace(9, WATER, "water");
        registry.registerInPlace(10, FLOWING_LAVA, "flowing_lava");
        registry.registerInPlace(11, LAVA, "lava");
        registry.registerInPlace(12, SAND, "sand");
        registry.registerInPlace(13, GRAVEL, "gravel");
        registry.registerInPlace(14, GOLD_ORE, "gold_ore");
        registry.registerInPlace(15, IRON_ORE, "iron_ore");
        registry.registerInPlace(16, COAL_ORE, "coal_ore");
        registry.registerInPlace(17, OAK_LOG, "log");
        registry.registerInPlace(18, OAK_LEAVES, "leaves");
        registry.registerInPlace(19, SPONGE, "sponge");
        registry.registerInPlace(20, GLASS, "glass");
        registry.registerInPlace(21, LAPIS_ORE, "lapis_ore");
        registry.registerInPlace(22, LAPIS_BLOCK, "lapis_block");
        registry.registerInPlace(23, DISPENSER, "dispenser");
        registry.registerInPlace(24, SANDSTONE, "sandstone");
        registry.registerInPlace(25, NOTE_BLOCK, "noteblock");
        registry.registerInPlace(26, WHITE_BED, "bed");
        registry.registerInPlace(27, POWERED_RAIL, "golden_rail");
        registry.registerInPlace(28, DETECTOR_RAIL, "detector_rail");
        registry.registerInPlace(29, STICKY_PISTON, "sticky_piston");
        registry.registerInPlace(30, COBWEB, "web");
        registry.registerInPlace(31, GRASS, "tallgrass");
        registry.registerInPlace(32, DEAD_BUSH, "deadbush");
        registry.registerInPlace(33, PISTON, "piston");
        registry.registerInPlace(34, PISTON_HEAD, "piston_head");
        registry.registerInPlace(35, WHITE_WOOL, "wool");
        registry.registerInPlace(36, MOVING_PISTON, "piston_extension");
        registry.registerInPlace(37, DANDELION, "yellow_flower");
        registry.registerInPlace(38, POPPY, "red_flower");
        registry.registerInPlace(39, BROWN_MUSHROOM, "brown_mushroom");
        registry.registerInPlace(40, RED_MUSHROOM, "red_mushroom");
        registry.registerInPlace(41, GOLD_BLOCK, "gold_block");
        registry.registerInPlace(42, IRON_BLOCK, "iron_block");
        registry.registerInPlace(43, DOUBLE_STONE_SLAB, "double_stone_slab");
        registry.registerInPlace(44, SMOOTH_STONE_SLAB, "stone_slab");
        registry.registerInPlace(45, BRICKS, "brick_block");
        registry.registerInPlace(46, TNT, "tnt");
        registry.registerInPlace(47, BOOKSHELF, "bookshelf");
        registry.registerInPlace(48, MOSSY_COBBLESTONE, "mossy_cobblestone");
        registry.registerInPlace(49, OBSIDIAN, "obsidian");
        registry.registerInPlace(50, TORCH, "torch");
        registry.registerInPlace(51, FIRE, "fire");
        registry.registerInPlace(52, SPAWNER, "mob_spawner");
        registry.registerInPlace(53, OAK_STAIRS, "oak_stairs");
        registry.registerInPlace(54, CHEST, "chest");
        registry.registerInPlace(55, REDSTONE_WIRE, "redstone_wire");
        registry.registerInPlace(56, DIAMOND_ORE, "diamond_ore");
        registry.registerInPlace(57, DIAMOND_BLOCK, "diamond_block");
        registry.registerInPlace(58, CRAFTING_TABLE, "crafting_table");
        registry.registerInPlace(59, WHEAT, "wheat");
        registry.registerInPlace(60, FARMLAND, "farmland");
        registry.registerInPlace(61, FURNACE, "furnace");
        registry.registerInPlace(62, LIT_FURNACE, "lit_furnace");
        registry.registerInPlace(63, OAK_SIGN, "standing_sign");
        registry.registerInPlace(64, OAK_DOOR, "wooden_door");
        registry.registerInPlace(65, LADDER, "ladder");
        registry.registerInPlace(66, RAIL, "rail");
        registry.registerInPlace(67, COBBLESTONE_STAIRS, "stone_stairs");
        registry.registerInPlace(68, OAK_WALL_SIGN, "wall_sign");
        registry.registerInPlace(69, LEVER, "lever");
        registry.registerInPlace(70, STONE_PRESSURE_PLATE, "stone_pressure_plate");
        registry.registerInPlace(71, IRON_DOOR, "iron_door");
        registry.registerInPlace(72, OAK_PRESSURE_PLATE, "wooden_pressure_plate");
        registry.registerInPlace(73, REDSTONE_ORE, "redstone_ore");
        registry.registerInPlace(74, LIT_REDSTONE_ORE, "lit_redstone_ore");
        registry.registerInPlace(75, UNLIT_REDSTONE_TORCH, "unlit_redstone_torch");
        registry.registerInPlace(76, REDSTONE_TORCH, "redstone_torch");
        registry.registerInPlace(77, STONE_BUTTON, "stone_button");
        registry.registerInPlace(78, SNOW, "snow_layer");
        registry.registerInPlace(79, ICE, "ice");
        registry.registerInPlace(80, SNOW_BLOCK, "snow");
        registry.registerInPlace(81, CACTUS, "cactus");
        registry.registerInPlace(82, CLAY, "clay");
        registry.registerInPlace(83, SUGAR_CANE, "reeds");
        registry.registerInPlace(84, JUKEBOX, "jukebox");
        registry.registerInPlace(85, OAK_FENCE, "fence");
        registry.registerInPlace(86, CARVED_PUMPKIN, "pumpkin");
        registry.registerInPlace(87, NETHERRACK, "netherrack");
        registry.registerInPlace(88, SOUL_SAND, "soul_sand");
        registry.registerInPlace(89, GLOWSTONE, "glowstone");
        registry.registerInPlace(90, NETHER_PORTAL, "portal");
        registry.registerInPlace(91, JACK_O_LANTERN, "lit_pumpkin");
        registry.registerInPlace(92, CAKE, "cake");
        registry.registerInPlace(93, REPEATER, "unpowered_repeater");
        registry.registerInPlace(94, POWERED_REPEATER, "powered_repeater");
        registry.registerInPlace(95, WHITE_STAINED_GLASS, "stained_glass");
        registry.registerInPlace(96, OAK_TRAPDOOR, "trapdoor");
        registry.registerInPlace(97, INFESTED_STONE, "monster_egg");
        registry.registerInPlace(98, STONE_BRICKS, "stonebrick");
        registry.registerInPlace(99, BROWN_MUSHROOM_BLOCK, "brown_mushroom_block");
        registry.registerInPlace(100, RED_MUSHROOM_BLOCK, "red_mushroom_block");
        registry.registerInPlace(101, IRON_BARS, "iron_bars");
        registry.registerInPlace(102, GLASS_PANE, "glass_pane");
        registry.registerInPlace(103, MELON, "melon_block");
        registry.registerInPlace(104, PUMPKIN_STEM, "pumpkin_stem");
        registry.registerInPlace(105, MELON_STEM, "melon_stem");
        registry.registerInPlace(106, VINE, "vine");
        registry.registerInPlace(107, OAK_FENCE_GATE, "fence_gate");
        registry.registerInPlace(108, BRICK_STAIRS, "brick_stairs");
        registry.registerInPlace(109, STONE_BRICK_STAIRS, "stone_brick_stairs");
        registry.registerInPlace(110, MYCELIUM, "mycelium");
        registry.registerInPlace(111, LILY_PAD, "waterlily");
        registry.registerInPlace(112, NETHER_BRICKS, "nether_brick");
        registry.registerInPlace(113, NETHER_BRICK_FENCE, "nether_brick_fence");
        registry.registerInPlace(114, NETHER_BRICK_STAIRS, "nether_brick_stairs");
        registry.registerInPlace(115, NETHER_WART, "nether_wart");
        registry.registerInPlace(116, ENCHANTING_TABLE, "enchanting_table");
        registry.registerInPlace(117, BREWING_STAND, "brewing_stand");
        registry.registerInPlace(118, CAULDRON, "cauldron");
        registry.registerInPlace(119, END_PORTAL, "end_portal");
        registry.registerInPlace(120, END_PORTAL_FRAME, "end_portal_frame");
        registry.registerInPlace(121, END_STONE, "end_stone");
        registry.registerInPlace(122, DRAGON_EGG, "dragon_egg");
        registry.registerInPlace(123, REDSTONE_LAMP, "redstone_lamp");
        registry.registerInPlace(124, LIT_REDSTONE_LAMP, "lit_redstone_lamp");
        registry.registerInPlace(125, DOUBLE_WOODEN_SLAB, "double_wooden_slab");
        registry.registerInPlace(126, OAK_SLAB, "wooden_slab");
        registry.registerInPlace(127, COCOA, "cocoa");
        registry.registerInPlace(128, SANDSTONE_STAIRS, "sandstone_stairs");
        registry.registerInPlace(129, EMERALD_ORE, "emerald_ore");
        registry.registerInPlace(130, ENDER_CHEST, "ender_chest");
        registry.registerInPlace(131, TRIPWIRE_HOOK, "tripwire_hook");
        registry.registerInPlace(132, TRIPWIRE, "tripwire");
        registry.registerInPlace(133, EMERALD_BLOCK, "emerald_block");
        registry.registerInPlace(134, SPRUCE_STAIRS, "spruce_stairs");
        registry.registerInPlace(135, BIRCH_STAIRS, "birch_stairs");
        registry.registerInPlace(136, JUNGLE_STAIRS, "jungle_stairs");
        registry.registerInPlace(137, COMMAND_BLOCK, "command_block");
        registry.registerInPlace(138, BEACON, "beacon");
        registry.registerInPlace(139, COBBLESTONE_WALL, "cobblestone_wall");
        registry.registerInPlace(140, FLOWER_POT, "flower_pot");
        registry.registerInPlace(141, CARROTS, "carrots");
        registry.registerInPlace(142, POTATOES, "potatoes");
        registry.registerInPlace(143, OAK_BUTTON, "wooden_button");
        registry.registerInPlace(144, SKELETON_SKULL, "skull");
        registry.registerInPlace(145, ANVIL, "anvil");
        registry.registerInPlace(146, TRAPPED_CHEST, "trapped_chest");
        registry.registerInPlace(147, LIGHT_WEIGHTED_PRESSURE_PLATE, "light_weighted_pressure_plate");
        registry.registerInPlace(148, HEAVY_WEIGHTED_PRESSURE_PLATE, "heavy_weighted_pressure_plate");
        registry.registerInPlace(149, COMPARATOR, "unpowered_comparator");
        registry.registerInPlace(150, POWERED_COMPARATOR, "powered_comparator");
        registry.registerInPlace(151, DAYLIGHT_DETECTOR, "daylight_detector");
        registry.registerInPlace(152, REDSTONE_BLOCK, "redstone_block");
        registry.registerInPlace(153, NETHER_QUARTZ_ORE, "quartz_ore");
        registry.registerInPlace(154, HOPPER, "hopper");
        registry.registerInPlace(155, QUARTZ_BLOCK, "quartz_block");
        registry.registerInPlace(156, QUARTZ_STAIRS, "quartz_stairs");
        registry.registerInPlace(157, ACTIVATOR_RAIL, "activator_rail");
        registry.registerInPlace(158, DROPPER, "dropper");
        registry.registerInPlace(159, WHITE_TERRACOTTA, "stained_hardened_clay");
        registry.registerInPlace(160, WHITE_STAINED_GLASS_PANE, "stained_glass_pane");
        registry.registerInPlace(161, ACACIA_LEAVES, "leaves2");
        registry.registerInPlace(162, ACACIA_LOG, "log2");
        registry.registerInPlace(163, ACACIA_STAIRS, "acacia_stairs");
        registry.registerInPlace(164, DARK_OAK_STAIRS, "dark_oak_stairs");
        registry.registerInPlace(165, SLIME_BLOCK, "slime");
        registry.registerInPlace(166, BARRIER, "barrier");
        registry.registerInPlace(167, IRON_TRAPDOOR, "iron_trapdoor");
        registry.registerInPlace(168, PRISMARINE, "prismarine");
        registry.registerInPlace(169, SEA_LANTERN, "sea_lantern");
        registry.registerInPlace(170, HAY_BLOCK, "hay_block");
        registry.registerInPlace(171, WHITE_CARPET, "carpet");
        registry.registerInPlace(172, TERRACOTTA, "hardened_clay");
        registry.registerInPlace(173, COAL_BLOCK, "coal_block");
        registry.registerInPlace(174, PACKED_ICE, "packed_ice");
        registry.registerInPlace(175, SUNFLOWER, "double_plant");
        registry.registerInPlace(176, BLACK_BANNER, "standing_banner");
        registry.registerInPlace(177, BLACK_WALL_BANNER, "wall_banner");
        registry.registerInPlace(178, DAYLIGHT_DETECTOR_INVERTED, "daylight_detector_inverted");
        registry.registerInPlace(179, RED_SANDSTONE, "red_sandstone");
        registry.registerInPlace(180, RED_SANDSTONE_STAIRS, "red_sandstone_stairs");
        registry.registerInPlace(181, DOUBLE_STONE_SLAB2, "double_stone_slab2");
        registry.registerInPlace(182, RED_SANDSTONE_SLAB, "stone_slab2");
        registry.registerInPlace(183, SPRUCE_FENCE_GATE, "spruce_fence_gate");
        registry.registerInPlace(184, BIRCH_FENCE_GATE, "birch_fence_gate");
        registry.registerInPlace(185, JUNGLE_FENCE_GATE, "jungle_fence_gate");
        registry.registerInPlace(186, DARK_OAK_FENCE_GATE, "dark_oak_fence_gate");
        registry.registerInPlace(187, ACACIA_FENCE_GATE, "acacia_fence_gate");
        registry.registerInPlace(188, SPRUCE_FENCE, "spruce_fence");
        registry.registerInPlace(189, BIRCH_FENCE, "birch_fence");
        registry.registerInPlace(190, JUNGLE_FENCE, "jungle_fence");
        registry.registerInPlace(191, DARK_OAK_FENCE, "dark_oak_fence");
        registry.registerInPlace(192, ACACIA_FENCE, "acacia_fence");
        registry.registerInPlace(193, SPRUCE_DOOR, "spruce_door");
        registry.registerInPlace(194, BIRCH_DOOR, "birch_door");
        registry.registerInPlace(195, JUNGLE_DOOR, "jungle_door");
        registry.registerInPlace(196, ACACIA_DOOR, "acacia_door");
        registry.registerInPlace(197, DARK_OAK_DOOR, "dark_oak_door");
        registry.registerInPlace(198, END_ROD, "end_rod");
        registry.registerInPlace(199, CHORUS_PLANT, "chorus_plant");
        registry.registerInPlace(200, CHORUS_FLOWER, "chorus_flower");
        registry.registerInPlace(201, PURPUR_BLOCK, "purpur_block");
        registry.registerInPlace(202, PURPUR_PILLAR, "purpur_pillar");
        registry.registerInPlace(203, PURPUR_STAIRS, "purpur_stairs");
        registry.registerInPlace(204, PURPUR_DOUBLE_SLAB, "purpur_double_slab");
        registry.registerInPlace(205, PURPUR_SLAB, "purpur_slab");
        registry.registerInPlace(206, END_STONE_BRICKS, "end_bricks");
        registry.registerInPlace(207, BEETROOTS, "beetroots");
        registry.registerInPlace(208, DIRT_PATH, "grass_path");
        registry.registerInPlace(209, END_GATEWAY, "end_gateway");
        registry.registerInPlace(210, REPEATING_COMMAND_BLOCK, "repeating_command_block");
        registry.registerInPlace(211, CHAIN_COMMAND_BLOCK, "chain_command_block");
        registry.registerInPlace(212, FROSTED_ICE, "frosted_ice");
        registry.registerInPlace(213, MAGMA_BLOCK, "magma");
        registry.registerInPlace(214, NETHER_WART_BLOCK, "nether_wart_block");
        registry.registerInPlace(215, RED_NETHER_BRICKS, "red_nether_brick");
        registry.registerInPlace(216, BONE_BLOCK, "bone_block");
        registry.registerInPlace(217, STRUCTURE_VOID, "structure_void");
        registry.registerInPlace(218, OBSERVER, "observer");
        registry.registerInPlace(219, WHITE_SHULKER_BOX, "white_shulker_box");
        registry.registerInPlace(220, ORANGE_SHULKER_BOX, "orange_shulker_box");
        registry.registerInPlace(221, MAGENTA_SHULKER_BOX, "magenta_shulker_box");
        registry.registerInPlace(222, LIGHT_BLUE_SHULKER_BOX, "light_blue_shulker_box");
        registry.registerInPlace(223, YELLOW_SHULKER_BOX, "yellow_shulker_box");
        registry.registerInPlace(224, LIME_SHULKER_BOX, "lime_shulker_box");
        registry.registerInPlace(225, PINK_SHULKER_BOX, "pink_shulker_box");
        registry.registerInPlace(226, GRAY_SHULKER_BOX, "gray_shulker_box");
        registry.registerInPlace(227, LIGHT_GRAY_SHULKER_BOX, "silver_shulker_box");
        registry.registerInPlace(228, CYAN_SHULKER_BOX, "cyan_shulker_box");
        registry.registerInPlace(229, PURPLE_SHULKER_BOX, "purple_shulker_box");
        registry.registerInPlace(230, BLUE_SHULKER_BOX, "blue_shulker_box");
        registry.registerInPlace(231, BROWN_SHULKER_BOX, "brown_shulker_box");
        registry.registerInPlace(232, GREEN_SHULKER_BOX, "green_shulker_box");
        registry.registerInPlace(233, RED_SHULKER_BOX, "red_shulker_box");
        registry.registerInPlace(234, BLACK_SHULKER_BOX, "black_shulker_box");
        registry.registerInPlace(235, WHITE_GLAZED_TERRACOTTA, "white_glazed_terracotta");
        registry.registerInPlace(236, ORANGE_GLAZED_TERRACOTTA, "orange_glazed_terracotta");
        registry.registerInPlace(237, MAGENTA_GLAZED_TERRACOTTA, "magenta_glazed_terracotta");
        registry.registerInPlace(238, LIGHT_BLUE_GLAZED_TERRACOTTA, "light_blue_glazed_terracotta");
        registry.registerInPlace(239, YELLOW_GLAZED_TERRACOTTA, "yellow_glazed_terracotta");
        registry.registerInPlace(240, LIME_GLAZED_TERRACOTTA, "lime_glazed_terracotta");
        registry.registerInPlace(241, PINK_GLAZED_TERRACOTTA, "pink_glazed_terracotta");
        registry.registerInPlace(242, GRAY_GLAZED_TERRACOTTA, "gray_glazed_terracotta");
        registry.registerInPlace(243, LIGHT_GRAY_GLAZED_TERRACOTTA, "silver_glazed_terracotta");
        registry.registerInPlace(244, CYAN_GLAZED_TERRACOTTA, "cyan_glazed_terracotta");
        registry.registerInPlace(245, PURPLE_GLAZED_TERRACOTTA, "purple_glazed_terracotta");
        registry.registerInPlace(246, BLUE_GLAZED_TERRACOTTA, "blue_glazed_terracotta");
        registry.registerInPlace(247, BROWN_GLAZED_TERRACOTTA, "brown_glazed_terracotta");
        registry.registerInPlace(248, GREEN_GLAZED_TERRACOTTA, "green_glazed_terracotta");
        registry.registerInPlace(249, RED_GLAZED_TERRACOTTA, "red_glazed_terracotta");
        registry.registerInPlace(250, BLACK_GLAZED_TERRACOTTA, "black_glazed_terracotta");
        registry.registerInPlace(251, WHITE_CONCRETE, "concrete");
        registry.registerInPlace(252, WHITE_CONCRETE_POWDER, "concrete_powder");
        registry.registerInPlace(255, STRUCTURE_BLOCK, "structure_block");

        registerAliases(registry);

        registry.enableSideEffects();
    }

}
