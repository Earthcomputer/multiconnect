package net.earthcomputer.multiconnect.protocols.v1_17_1;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;

public final class Biomes_1_17_1 {
    private Biomes_1_17_1() {}


    public static final RegistryKey<Biome> SNOWY_MOUNTAINS = create("snowy_mountains");
    public static final RegistryKey<Biome> MUSHROOM_FIELD_SHORE = create("mushroom_field_shore");
    public static final RegistryKey<Biome> DESERT_HILLS = create("desert_hills");
    public static final RegistryKey<Biome> WOODED_HILLS = create("wooded_hills");
    public static final RegistryKey<Biome> MOUNTAIN_EDGE = create("mountain_edge");
    public static final RegistryKey<Biome> TAIGA_HILLS = create("taiga_hills");
    public static final RegistryKey<Biome> JUNGLE_HILLS = create("jungle_hills");
    public static final RegistryKey<Biome> BIRCH_FOREST_HILLS = create("birch_forest_hills");
    public static final RegistryKey<Biome> SNOWY_TAIGA_HILLS = create("snowy_taiga_hills");
    public static final RegistryKey<Biome> GIANT_TREE_TAIGA_HILLS = create("giant_tree_taiga_hills");
    public static final RegistryKey<Biome> BADLANDS_PLATEAU = create("badlands_plateau");
    public static final RegistryKey<Biome> DESERT_LAKES = create("desert_lakes");
    public static final RegistryKey<Biome> TAIGA_MOUNTAINS = create("taiga_mountains");
    public static final RegistryKey<Biome> SWAMP_HILLS = create("swamp_hills");
    public static final RegistryKey<Biome> MODIFIED_JUNGLE = create("modified_jungle");
    public static final RegistryKey<Biome> MODIFIED_JUNGLE_EDGE = create("modified_jungle_edge");
    public static final RegistryKey<Biome> TALL_BIRCH_HILLS = create("tall_birch_hills");
    public static final RegistryKey<Biome> DARK_FOREST_HILLS = create("dark_forest_hills");
    public static final RegistryKey<Biome> SNOWY_TAIGA_MOUNTAINS = create("snowy_taiga_mountains");
    public static final RegistryKey<Biome> GIANT_SPRUCE_TAIGA_HILLS = create("giant_spruce_taiga_hills");
    public static final RegistryKey<Biome> MODIFIED_GRAVELLY_MOUNTAINS = create("modified_gravelly_mountains");
    public static final RegistryKey<Biome> SHATTERED_SAVANNA_PLATEAU = create("shattered_savanna_plateau");
    public static final RegistryKey<Biome> MODIFIED_WOODED_BADLANDS_PLATEAU = create("modified_wooded_badlands_plateau");
    public static final RegistryKey<Biome> MODIFIED_BADLANDS_PLATEAU = create("modified_badlands_plateau");
    public static final RegistryKey<Biome> BAMBOO_JUNGLE_HILLS = create("bamboo_jungle_hills");
    public static final RegistryKey<Biome> DEEP_WARM_OCEAN = create("deep_warm_ocean");
    public static final RegistryKey<Biome> MOUNTAINS = create("mountains");
    public static final RegistryKey<Biome> SNOWY_TUNDRA = create("snowy_tundra");
    public static final RegistryKey<Biome> JUNGLE_EDGE = create("jungle_edge");
    public static final RegistryKey<Biome> STONE_SHORE = create("stone_shore");
    public static final RegistryKey<Biome> GIANT_TREE_TAIGA = create("giant_tree_taiga");
    public static final RegistryKey<Biome> WOODED_MOUNTAINS = create("wooded_mountains");
    public static final RegistryKey<Biome> WOODED_BADLANDS_PLATEAU = create("wooded_badlands_plateau");
    public static final RegistryKey<Biome> GRAVELLY_MOUNTAINS = create("gravelly_mountains");
    public static final RegistryKey<Biome> TALL_BIRCH_FOREST = create("tall_birch_forest");
    public static final RegistryKey<Biome> GIANT_SPRUCE_TAIGA = create("giant_spruce_taiga");
    public static final RegistryKey<Biome> SHATTERED_SAVANNA = create("shattered_savanna");

    private static RegistryKey<Biome> create(String id) {
        return RegistryKey.of(Registry.BIOME_KEY, new Identifier(id));
    }
}
