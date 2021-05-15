package net.earthcomputer.multiconnect.protocols.v1_14_4;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BuiltinBiomes;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeAccessType;

public final class BiomeAccessType_1_14_4 implements BiomeAccessType {

    public static final BiomeAccessType_1_14_4 INSTANCE = new BiomeAccessType_1_14_4();

    private BiomeAccessType_1_14_4() {}

    @Override
    public Biome getBiome(long seed, int x, int y, int z, BiomeAccess.Storage storage) {
        Biome biome = null;
        if (storage instanceof IBiomeStorage_1_14_4 storage114) {
            biome = storage114.multiconnect_getBiome_1_14_4(x, z);
        }
        if (biome == null) {
            return BuiltinBiomes.PLAINS;
        }
        return biome;
    }
}
