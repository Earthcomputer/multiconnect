package net.earthcomputer.multiconnect.protocols.v1_14_4.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_14_4.BiomeAccess_1_14_4;
import net.earthcomputer.multiconnect.protocols.v1_14_4.IBiomeStorage_1_14_4;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccessType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(World.class)
public abstract class MixinWorld implements IBiomeStorage_1_14_4 {

    @Shadow public abstract Chunk getChunk(int x, int z, ChunkStatus status, boolean boolean_1);

    @ModifyArg(method = "<init>", index = 2, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/source/BiomeAccess;<init>(Lnet/minecraft/world/biome/source/BiomeAccess$Storage;JLnet/minecraft/world/biome/source/BiomeAccessType;)V"))
    private BiomeAccessType modifyBiomeAccessType(BiomeAccessType _default) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_14_4)
            return BiomeAccess_1_14_4.INSTANCE;
        else
            return _default;
    }

    @Override
    public Biome multiconnect_getBiome_1_14_4(int x, int z) {
        Chunk chunk = getChunk(x >> 4, z >> 4, ChunkStatus.BIOMES, false);
        if (chunk instanceof IBiomeStorage_1_14_4)
            return ((IBiomeStorage_1_14_4) chunk).multiconnect_getBiome_1_14_4(x & 15, z & 15);
        return null;
    }

    @Override
    public void multiconnect_setBiomeArray_1_14_4(Biome[] biomes) {
        throw new UnsupportedOperationException();
    }
}
