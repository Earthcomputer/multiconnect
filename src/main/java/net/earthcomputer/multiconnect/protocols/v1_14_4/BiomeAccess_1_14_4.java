package net.earthcomputer.multiconnect.protocols.v1_14_4;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.ColumnFuzzedBiomeMagnifier;
import net.minecraft.world.biome.IBiomeMagnifier;

public final class BiomeAccess_1_14_4 implements IBiomeMagnifier {

    public static final BiomeAccess_1_14_4 INSTANCE = new BiomeAccess_1_14_4();

    private BiomeAccess_1_14_4() {}

    @Override
    public Biome getBiome(long seed, int x, int y, int z, BiomeManager.IBiomeReader storage) {
        Biome biome = null;
        if (storage instanceof IBiomeStorage_1_14_4) {
            biome = ((IBiomeStorage_1_14_4) storage).multiconnect_getBiome_1_14_4(x, z);
        }
        if (biome == null) {
            return ColumnFuzzedBiomeMagnifier.INSTANCE.getBiome(seed, x, y, z, storage);
        }
        return biome;
    }
}
