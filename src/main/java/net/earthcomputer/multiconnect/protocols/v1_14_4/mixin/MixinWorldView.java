package net.earthcomputer.multiconnect.protocols.v1_14_4.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WorldView.class)
public interface MixinWorldView {
    @Shadow Chunk getChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create);
    @Shadow Biome getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ);
    @Shadow Biome getBiome(BlockPos pos);

    /**
     * TODO: replace with @Inject once this happens
     *
     * @reason Mixin doesn't support @Inject in interfaces yet.
     * @author Earthcomputer
     */
    @Overwrite
    default Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
        // multiconnect start
        if (ConnectionInfo.protocolVersion <= Protocols.V1_14_4) {
            return getBiome(new BlockPos(biomeX, biomeY, biomeZ));
        }
        // multiconnect end

        Chunk chunk = this.getChunk(biomeX >> 2, biomeZ >> 2, ChunkStatus.BIOMES, false);
        return chunk != null && chunk.getBiomeArray() != null ? chunk.getBiomeArray().getBiomeForNoiseGen(biomeX, biomeY, biomeZ) : this.getGeneratorStoredBiome(biomeX, biomeY, biomeZ);
    }
}
