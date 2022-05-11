package net.earthcomputer.multiconnect.protocols.v1_17_1;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.protocols.generic.Key;
import net.earthcomputer.multiconnect.protocols.v1_18.Protocol_1_18;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import java.util.BitSet;

public class Protocol_1_17_1 extends Protocol_1_18 {
    public static final Key<BitSet> VERTICAL_STRIP_BITMASK = Key.create("verticalStripBitmask");
    public static final Key<IntList> BIOMES = Key.create("biomes");

    private static final Int2ObjectMap<RegistryKey<Biome>> OLD_BIOME_IDS = new Int2ObjectOpenHashMap<>();
    static {
        OLD_BIOME_IDS.put(0, BiomeKeys.OCEAN);
        OLD_BIOME_IDS.put(1, BiomeKeys.PLAINS);
        OLD_BIOME_IDS.put(2, BiomeKeys.DESERT);
        OLD_BIOME_IDS.put(3, BiomeKeys.WINDSWEPT_HILLS);
        OLD_BIOME_IDS.put(4, BiomeKeys.FOREST);
        OLD_BIOME_IDS.put(5, BiomeKeys.TAIGA);
        OLD_BIOME_IDS.put(6, BiomeKeys.SWAMP);
        OLD_BIOME_IDS.put(7, BiomeKeys.RIVER);
        OLD_BIOME_IDS.put(8, BiomeKeys.NETHER_WASTES);
        OLD_BIOME_IDS.put(9, BiomeKeys.THE_END);
        OLD_BIOME_IDS.put(10, BiomeKeys.FROZEN_OCEAN);
        OLD_BIOME_IDS.put(11, BiomeKeys.FROZEN_RIVER);
        OLD_BIOME_IDS.put(12, BiomeKeys.SNOWY_PLAINS);
        OLD_BIOME_IDS.put(13, Biomes_1_17_1.SNOWY_MOUNTAINS);
        OLD_BIOME_IDS.put(14, BiomeKeys.MUSHROOM_FIELDS);
        OLD_BIOME_IDS.put(15, Biomes_1_17_1.MUSHROOM_FIELD_SHORE);
        OLD_BIOME_IDS.put(16, BiomeKeys.BEACH);
        OLD_BIOME_IDS.put(17, Biomes_1_17_1.DESERT_HILLS);
        OLD_BIOME_IDS.put(18, Biomes_1_17_1.WOODED_HILLS);
        OLD_BIOME_IDS.put(19, Biomes_1_17_1.TAIGA_HILLS);
        OLD_BIOME_IDS.put(20, Biomes_1_17_1.MOUNTAIN_EDGE);
        OLD_BIOME_IDS.put(21, BiomeKeys.JUNGLE);
        OLD_BIOME_IDS.put(22, Biomes_1_17_1.JUNGLE_HILLS);
        OLD_BIOME_IDS.put(23, BiomeKeys.SPARSE_JUNGLE);
        OLD_BIOME_IDS.put(24, BiomeKeys.DEEP_OCEAN);
        OLD_BIOME_IDS.put(25, BiomeKeys.STONY_SHORE);
        OLD_BIOME_IDS.put(26, BiomeKeys.SNOWY_BEACH);
        OLD_BIOME_IDS.put(27, BiomeKeys.BIRCH_FOREST);
        OLD_BIOME_IDS.put(28, Biomes_1_17_1.BIRCH_FOREST_HILLS);
        OLD_BIOME_IDS.put(29, BiomeKeys.DARK_FOREST);
        OLD_BIOME_IDS.put(30, BiomeKeys.SNOWY_TAIGA);
        OLD_BIOME_IDS.put(31, Biomes_1_17_1.SNOWY_TAIGA_HILLS);
        OLD_BIOME_IDS.put(32, BiomeKeys.OLD_GROWTH_PINE_TAIGA);
        OLD_BIOME_IDS.put(33, Biomes_1_17_1.GIANT_TREE_TAIGA_HILLS);
        OLD_BIOME_IDS.put(34, BiomeKeys.WINDSWEPT_FOREST);
        OLD_BIOME_IDS.put(35, BiomeKeys.SAVANNA);
        OLD_BIOME_IDS.put(36, BiomeKeys.SAVANNA_PLATEAU);
        OLD_BIOME_IDS.put(37, BiomeKeys.BADLANDS);
        OLD_BIOME_IDS.put(38, BiomeKeys.WOODED_BADLANDS);
        OLD_BIOME_IDS.put(39, Biomes_1_17_1.BADLANDS_PLATEAU);
        OLD_BIOME_IDS.put(40, BiomeKeys.SMALL_END_ISLANDS);
        OLD_BIOME_IDS.put(41, BiomeKeys.END_MIDLANDS);
        OLD_BIOME_IDS.put(42, BiomeKeys.END_HIGHLANDS);
        OLD_BIOME_IDS.put(43, BiomeKeys.END_BARRENS);
        OLD_BIOME_IDS.put(44, BiomeKeys.WARM_OCEAN);
        OLD_BIOME_IDS.put(45, BiomeKeys.LUKEWARM_OCEAN);
        OLD_BIOME_IDS.put(46, BiomeKeys.COLD_OCEAN);
        OLD_BIOME_IDS.put(47, Biomes_1_17_1.DEEP_WARM_OCEAN);
        OLD_BIOME_IDS.put(48, BiomeKeys.DEEP_LUKEWARM_OCEAN);
        OLD_BIOME_IDS.put(49, BiomeKeys.DEEP_COLD_OCEAN);
        OLD_BIOME_IDS.put(50, BiomeKeys.DEEP_FROZEN_OCEAN);
        OLD_BIOME_IDS.put(129, BiomeKeys.SUNFLOWER_PLAINS);
        OLD_BIOME_IDS.put(130, Biomes_1_17_1.DESERT_LAKES);
        OLD_BIOME_IDS.put(131, BiomeKeys.WINDSWEPT_GRAVELLY_HILLS);
        OLD_BIOME_IDS.put(132, BiomeKeys.FLOWER_FOREST);
        OLD_BIOME_IDS.put(133, Biomes_1_17_1.TAIGA_MOUNTAINS);
        OLD_BIOME_IDS.put(134, Biomes_1_17_1.SWAMP_HILLS);
        OLD_BIOME_IDS.put(140, BiomeKeys.ICE_SPIKES);
        OLD_BIOME_IDS.put(149, Biomes_1_17_1.MODIFIED_JUNGLE);
        OLD_BIOME_IDS.put(151, Biomes_1_17_1.MODIFIED_JUNGLE_EDGE);
        OLD_BIOME_IDS.put(155, BiomeKeys.OLD_GROWTH_BIRCH_FOREST);
        OLD_BIOME_IDS.put(156, Biomes_1_17_1.TALL_BIRCH_HILLS);
        OLD_BIOME_IDS.put(157, Biomes_1_17_1.DARK_FOREST_HILLS);
        OLD_BIOME_IDS.put(158, Biomes_1_17_1.SNOWY_TAIGA_MOUNTAINS);
        OLD_BIOME_IDS.put(160, BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA);
        OLD_BIOME_IDS.put(161, Biomes_1_17_1.GIANT_SPRUCE_TAIGA_HILLS);
        OLD_BIOME_IDS.put(162, Biomes_1_17_1.MODIFIED_GRAVELLY_MOUNTAINS);
        OLD_BIOME_IDS.put(163, BiomeKeys.WINDSWEPT_SAVANNA);
        OLD_BIOME_IDS.put(164, Biomes_1_17_1.SHATTERED_SAVANNA_PLATEAU);
        OLD_BIOME_IDS.put(165, BiomeKeys.ERODED_BADLANDS);
        OLD_BIOME_IDS.put(166, Biomes_1_17_1.MODIFIED_WOODED_BADLANDS_PLATEAU);
        OLD_BIOME_IDS.put(167, Biomes_1_17_1.MODIFIED_BADLANDS_PLATEAU);
        OLD_BIOME_IDS.put(168, BiomeKeys.BAMBOO_JUNGLE);
        OLD_BIOME_IDS.put(169, Biomes_1_17_1.BAMBOO_JUNGLE_HILLS);
        OLD_BIOME_IDS.put(170, BiomeKeys.SOUL_SAND_VALLEY);
        OLD_BIOME_IDS.put(171, BiomeKeys.CRIMSON_FOREST);
        OLD_BIOME_IDS.put(172, BiomeKeys.WARPED_FOREST);
        OLD_BIOME_IDS.put(173, BiomeKeys.BASALT_DELTAS);
        OLD_BIOME_IDS.put(174, BiomeKeys.DRIPSTONE_CAVES);
        OLD_BIOME_IDS.put(175, BiomeKeys.LUSH_CAVES);
    }

    public static int mapBiomeId(int oldId, Registry<Biome> biomeRegistry) {
        RegistryKey<Biome> newKey = OLD_BIOME_IDS.get(oldId);
        if (newKey == null) {
            return oldId;
        }
        Biome biome = biomeRegistry.get(newKey);
        if (biome == null) {
            return oldId;
        }
        return biomeRegistry.getRawId(biome);
    }
}
