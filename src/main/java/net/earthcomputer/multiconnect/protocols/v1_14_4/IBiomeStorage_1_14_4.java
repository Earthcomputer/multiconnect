package net.earthcomputer.multiconnect.protocols.v1_14_4;

import net.minecraft.world.biome.Biome;

public interface IBiomeStorage_1_14_4 {

    void multiconnect_setBiomeArray_1_14_4(Biome[] biomes);

    Biome multiconnect_getBiome_1_14_4(int x, int z);

}
