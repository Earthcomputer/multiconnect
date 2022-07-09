package net.earthcomputer.multiconnect.protocols.v1_17;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public final class Biomes_1_17_1 {
    private Biomes_1_17_1() {}


    public static final ResourceKey<Biome> SNOWY_MOUNTAINS = create("snowy_mountains");
    public static final ResourceKey<Biome> MUSHROOM_FIELD_SHORE = create("mushroom_field_shore");
    public static final ResourceKey<Biome> DESERT_HILLS = create("desert_hills");
    public static final ResourceKey<Biome> WOODED_HILLS = create("wooded_hills");
    public static final ResourceKey<Biome> MOUNTAIN_EDGE = create("mountain_edge");
    public static final ResourceKey<Biome> TAIGA_HILLS = create("taiga_hills");
    public static final ResourceKey<Biome> JUNGLE_HILLS = create("jungle_hills");
    public static final ResourceKey<Biome> BIRCH_FOREST_HILLS = create("birch_forest_hills");
    public static final ResourceKey<Biome> SNOWY_TAIGA_HILLS = create("snowy_taiga_hills");
    public static final ResourceKey<Biome> GIANT_TREE_TAIGA_HILLS = create("giant_tree_taiga_hills");
    public static final ResourceKey<Biome> BADLANDS_PLATEAU = create("badlands_plateau");
    public static final ResourceKey<Biome> DESERT_LAKES = create("desert_lakes");
    public static final ResourceKey<Biome> TAIGA_MOUNTAINS = create("taiga_mountains");
    public static final ResourceKey<Biome> SWAMP_HILLS = create("swamp_hills");
    public static final ResourceKey<Biome> MODIFIED_JUNGLE = create("modified_jungle");
    public static final ResourceKey<Biome> MODIFIED_JUNGLE_EDGE = create("modified_jungle_edge");
    public static final ResourceKey<Biome> TALL_BIRCH_HILLS = create("tall_birch_hills");
    public static final ResourceKey<Biome> DARK_FOREST_HILLS = create("dark_forest_hills");
    public static final ResourceKey<Biome> SNOWY_TAIGA_MOUNTAINS = create("snowy_taiga_mountains");
    public static final ResourceKey<Biome> GIANT_SPRUCE_TAIGA_HILLS = create("giant_spruce_taiga_hills");
    public static final ResourceKey<Biome> MODIFIED_GRAVELLY_MOUNTAINS = create("modified_gravelly_mountains");
    public static final ResourceKey<Biome> SHATTERED_SAVANNA_PLATEAU = create("shattered_savanna_plateau");
    public static final ResourceKey<Biome> MODIFIED_WOODED_BADLANDS_PLATEAU = create("modified_wooded_badlands_plateau");
    public static final ResourceKey<Biome> MODIFIED_BADLANDS_PLATEAU = create("modified_badlands_plateau");
    public static final ResourceKey<Biome> BAMBOO_JUNGLE_HILLS = create("bamboo_jungle_hills");
    public static final ResourceKey<Biome> DEEP_WARM_OCEAN = create("deep_warm_ocean");
    public static final ResourceKey<Biome> MOUNTAINS = create("mountains");
    public static final ResourceKey<Biome> SNOWY_TUNDRA = create("snowy_tundra");
    public static final ResourceKey<Biome> JUNGLE_EDGE = create("jungle_edge");
    public static final ResourceKey<Biome> STONE_SHORE = create("stone_shore");
    public static final ResourceKey<Biome> GIANT_TREE_TAIGA = create("giant_tree_taiga");
    public static final ResourceKey<Biome> WOODED_MOUNTAINS = create("wooded_mountains");
    public static final ResourceKey<Biome> WOODED_BADLANDS_PLATEAU = create("wooded_badlands_plateau");
    public static final ResourceKey<Biome> GRAVELLY_MOUNTAINS = create("gravelly_mountains");
    public static final ResourceKey<Biome> TALL_BIRCH_FOREST = create("tall_birch_forest");
    public static final ResourceKey<Biome> GIANT_SPRUCE_TAIGA = create("giant_spruce_taiga");
    public static final ResourceKey<Biome> SHATTERED_SAVANNA = create("shattered_savanna");

    private static ResourceKey<Biome> create(String id) {
        return ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(id));
    }
}
