package net.earthcomputer.multiconnect.protocols.v1_14_4.mixin;

import net.earthcomputer.multiconnect.protocols.v1_14_4.IBiomeStorage_1_14_4;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(WorldChunk.class)
public class MixinWorldChunk implements IBiomeStorage_1_14_4 {
    @Shadow @Final World world;
    @Unique private Biome[] biomeArray_1_14_4;

    @Override
    public Biome multiconnect_getBiome_1_14_4(int x, int z) {
        if (biomeArray_1_14_4 == null) {
            return world.getRegistryManager().get(Registry.BIOME_KEY).get(BiomeKeys.PLAINS);
        }
        Biome biome = biomeArray_1_14_4[(z & 15) << 4 | (x & 15)];
        return biome != null ? biome : world.getRegistryManager().get(Registry.BIOME_KEY).get(BiomeKeys.PLAINS);
    }

    @Override
    public void multiconnect_setBiomeArray_1_14_4(Biome[] biomes) {
        biomeArray_1_14_4 = biomes;
    }
}
