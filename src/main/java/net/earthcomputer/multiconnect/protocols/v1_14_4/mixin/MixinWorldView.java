package net.earthcomputer.multiconnect.protocols.v1_14_4.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_14_4.BiomeAccessType_1_14_4;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@Mixin(WorldView.class)
public interface MixinWorldView {
    @SuppressWarnings("PublicStaticMixinMember")
    @Unique
    Logger MULTICONNECT_LOGGER = LogManager.getLogger("multiconnect");
    @SuppressWarnings("PublicStaticMixinMember")
    @Unique
    Set<WorldView> multiconnect_hasWarned = Collections.newSetFromMap(new WeakHashMap<>());

    @Shadow Chunk getChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create);
    @Shadow Biome getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ);
    @Shadow Biome getBiome(BlockPos pos);
    @Shadow BiomeAccess getBiomeAccess();

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
            if (((BiomeAccessAccessor) getBiomeAccess()).getType() == BiomeAccessType_1_14_4.INSTANCE) {
                return getBiome(new BlockPos(biomeX, biomeY, biomeZ));
            } else if (multiconnect_hasWarned.add((WorldView) this)) {
                MULTICONNECT_LOGGER.warn("Unsupported biome access type for 1.14 and below: {}. This class: {}",
                        ((BiomeAccessAccessor) getBiomeAccess()).getType().getClass().getName(),
                        this.getClass().getName());
            }
        }
        // multiconnect end

        Chunk chunk = this.getChunk(biomeX >> 2, biomeZ >> 2, ChunkStatus.BIOMES, false);
        return chunk != null && chunk.getBiomeArray() != null ? chunk.getBiomeArray().getBiomeForNoiseGen(biomeX, biomeY, biomeZ) : this.getGeneratorStoredBiome(biomeX, biomeY, biomeZ);
    }
}
