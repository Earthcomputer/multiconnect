package net.earthcomputer.multiconnect.protocols.v1_14_4.mixin;

import net.earthcomputer.multiconnect.protocols.v1_14_4.IBiomeStorage_1_14_4;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(WorldChunk.class)
public class MixinWorldChunk implements IBiomeStorage_1_14_4 {

    @Unique private Biome[] biomeArray_1_14_4;

    @Override
    public Biome multiconnect_getBiome_1_14_4(int x, int z) {
        if (biomeArray_1_14_4 == null) return null;
        return biomeArray_1_14_4[(z & 15) << 4 | (x & 15)];
    }

    @Override
    public void multiconnect_setBiomeArray_1_14_4(Biome[] biomes) {
        biomeArray_1_14_4 = biomes;
    }
}
