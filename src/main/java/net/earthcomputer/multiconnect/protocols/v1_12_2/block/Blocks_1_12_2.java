package net.earthcomputer.multiconnect.protocols.v1_12_2.block;

import net.minecraft.block.*;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.registry.Registry;

public class Blocks_1_12_2 {
    public static final Block FLOWING_WATER = new OldFluidBlock(Fluids.FLOWING_WATER, Blocks.WATER);
    public static final Block FLOWING_LAVA = new OldFluidBlock(Fluids.FLOWING_LAVA, Blocks.LAVA);
    public static final Block DOUBLE_STONE_SLAB = new OldBlock(Blocks.SMOOTH_STONE_SLAB);
    public static final Block LIT_FURNACE = new OldLitBlock(Blocks.FURNACE, true);
    public static final Block LIT_REDSTONE_ORE = new OldLitBlock(Blocks.REDSTONE_ORE, true);
    public static final Block UNLIT_REDSTONE_TORCH = new OldLitBlock(Blocks.REDSTONE_TORCH, false);
    public static final Block POWERED_REPEATER = new OldBlock(Blocks.REPEATER);
    public static final Block LIT_REDSTONE_LAMP = new OldLitBlock(Blocks.REDSTONE_LAMP, true);
    public static final Block DOUBLE_WOODEN_SLAB = new OldBlock(Blocks.OAK_SLAB);
    public static final Block POWERED_COMPARATOR = new OldComparatorBlock(Blocks.COMPARATOR);
    public static final Block DAYLIGHT_DETECTOR_INVERTED = new OldBlock(Blocks.DAYLIGHT_DETECTOR);
    public static final Block DOUBLE_STONE_SLAB2 = new OldBlock(Blocks.RED_SANDSTONE_SLAB);
    public static final Block PURPUR_DOUBLE_SLAB = new OldBlock(Blocks.PURPUR_SLAB);
    public static final Block OLD_DEAD_BUSH = new OldBlock(Blocks.DEAD_BUSH);
    public static final Block SEAMLESS_PETRIFIED_OAK_SLAB = new OldBlock(Blocks.PETRIFIED_OAK_SLAB);
    public static final Block SEAMLESS_COBBLESTONE_SLAB = new OldBlock(Blocks.COBBLESTONE_SLAB);
    public static final Block SEAMLESS_BRICK_SLAB = new OldBlock(Blocks.BRICK_SLAB);
    public static final Block STONE_BRICK_SLAB = new OldBlock(Blocks.STONE_BRICK_SLAB);
    public static final Block NETHER_BRICK_SLAB = new OldBlock(Blocks.NETHER_BRICK_SLAB);
    public static final Block BROWN_MUSHROOM_BLOCK_ALTERNATIVE = new Old3VariantBlock(Blocks.BROWN_MUSHROOM_BLOCK);
    public static final Block RED_MUSHROOM_BLOCK_ALTERNATIVE = new Old3VariantBlock(Blocks.RED_MUSHROOM_BLOCK);
    public static final Block RED_MUSHROOM_STEM = new Old2VariantBlock(Blocks.RED_MUSHROOM_BLOCK);
    public static final Block TRIPWIRE_ALTERNATIVE = new OldTripwireAlternativeBlock(Blocks.TRIPWIRE);
    public static final Block LEGACY_FLOWER_POT = new OldFlowerPotBlock(Blocks.FLOWER_POT);
    public static final Block NODROP_SKULL = new OldNodropSkullBlock(Blocks.SKELETON_SKULL);

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

    public static void register() {
        Registry.register(Registry.BLOCK, "multiconnect:flowing_water", FLOWING_WATER);
        Registry.register(Registry.BLOCK, "multiconnect:flowing_lava", FLOWING_LAVA);
        Registry.register(Registry.BLOCK, "multiconnect:double_stone_slab", DOUBLE_STONE_SLAB);
        Registry.register(Registry.BLOCK, "multiconnect:lit_furnace", LIT_FURNACE);
        Registry.register(Registry.BLOCK, "multiconnect:lit_redstone_ore", LIT_REDSTONE_ORE);
        Registry.register(Registry.BLOCK, "multiconnect:unlit_redstone_torch", UNLIT_REDSTONE_TORCH);
        Registry.register(Registry.BLOCK, "multiconnect:powered_repeater", POWERED_REPEATER);
        Registry.register(Registry.BLOCK, "multiconnect:lit_redstone_lamp", LIT_REDSTONE_LAMP);
        Registry.register(Registry.BLOCK, "multiconnect:double_wooden_slab", DOUBLE_WOODEN_SLAB);
        Registry.register(Registry.BLOCK, "multiconnect:powered_comparator", POWERED_COMPARATOR);
        Registry.register(Registry.BLOCK, "multiconnect:daylight_detector_inverted", DAYLIGHT_DETECTOR_INVERTED);
        Registry.register(Registry.BLOCK, "multiconnect:double_stone_slab2", DOUBLE_STONE_SLAB2);
        Registry.register(Registry.BLOCK, "multiconnect:purpur_double_slab", PURPUR_DOUBLE_SLAB);
        Registry.register(Registry.BLOCK, "multiconnect:old_dead_bush", OLD_DEAD_BUSH);
        Registry.register(Registry.BLOCK, "multiconnect:seamless_petrified_oak_slab", SEAMLESS_PETRIFIED_OAK_SLAB);
        Registry.register(Registry.BLOCK, "multiconnect:seamless_cobblestone_slab", SEAMLESS_COBBLESTONE_SLAB);
        Registry.register(Registry.BLOCK, "multiconnect:seamless_brick_slab", SEAMLESS_BRICK_SLAB);
        Registry.register(Registry.BLOCK, "multiconnect:stone_brick_slab", STONE_BRICK_SLAB);
        Registry.register(Registry.BLOCK, "multiconnect:nether_brick_slab", NETHER_BRICK_SLAB);
        Registry.register(Registry.BLOCK, "multiconnect:brown_mushroom_block_alternative", BROWN_MUSHROOM_BLOCK_ALTERNATIVE);
        Registry.register(Registry.BLOCK, "multiconnect:red_mushroom_block_alternative", RED_MUSHROOM_BLOCK_ALTERNATIVE);
        Registry.register(Registry.BLOCK, "multiconnect:red_mushroom_stem", RED_MUSHROOM_STEM);
        Registry.register(Registry.BLOCK, "multiconnect:tripwire_alternative", TRIPWIRE_ALTERNATIVE);
        Registry.register(Registry.BLOCK, "multiconnect:legacy_flower_pot", LEGACY_FLOWER_POT);
        Registry.register(Registry.BLOCK, "multiconnect:nodrop_skull", NODROP_SKULL);
    }
}
