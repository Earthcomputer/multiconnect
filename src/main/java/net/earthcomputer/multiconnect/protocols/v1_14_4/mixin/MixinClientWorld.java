package net.earthcomputer.multiconnect.protocols.v1_14_4.mixin;

import net.earthcomputer.multiconnect.protocols.v1_14_4.IBiomeStorage_1_14_4;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Supplier;

@Mixin(ClientWorld.class)
public abstract class MixinClientWorld extends World implements IBiomeStorage_1_14_4 {

    protected MixinClientWorld(MutableWorldProperties properties, RegistryKey<World> registryRef, DimensionType dimensionType, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed) {
        super(properties, registryRef, dimensionType, profiler, isClient, debugWorld, seed);
    }

    @Override
    public Biome multiconnect_getBiome_1_14_4(int x, int z) {
        Chunk chunk = getChunk(x >> 4, z >> 4, ChunkStatus.BIOMES, false);
        if (chunk instanceof IBiomeStorage_1_14_4 storage114) {
            return storage114.multiconnect_getBiome_1_14_4(x & 15, z & 15);
        }
        return getRegistryManager().get(Registry.BIOME_KEY).get(BiomeKeys.PLAINS);
    }

    @Override
    public void multiconnect_setBiomeArray_1_14_4(Biome[] biomes) {
        throw new UnsupportedOperationException();
    }
}
