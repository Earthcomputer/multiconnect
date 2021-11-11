package net.earthcomputer.multiconnect.protocols.v1_14_4.mixin;

import net.earthcomputer.multiconnect.protocols.v1_14_4.IBiomeStorage_1_14_4;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(World.class)
public abstract class MixinWorld implements IBiomeStorage_1_14_4 {

    @Shadow public abstract Chunk getChunk(int x, int z, ChunkStatus status, boolean boolean_1);

    @Override
    public Biome multiconnect_getBiome_1_14_4(int x, int z) {
        Chunk chunk = getChunk(x >> 4, z >> 4, ChunkStatus.BIOMES, false);
        if (chunk instanceof IBiomeStorage_1_14_4 storage114) {
            return storage114.multiconnect_getBiome_1_14_4(x & 15, z & 15);
        }
        return null;
    }

    @Override
    public void multiconnect_setBiomeArray_1_14_4(Biome[] biomes) {
        throw new UnsupportedOperationException();
    }
}
