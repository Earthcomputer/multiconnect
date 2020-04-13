package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_12_2.ChunkUpgrader;
import net.earthcomputer.multiconnect.protocols.v1_12_2.IUpgradableChunk;
import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientChunkProvider.class)
public abstract class MixinClientChunkManager {

    @Unique private static final Object LOCK = new Object();

    @Shadow public abstract Chunk getChunk(int x, int z, ChunkStatus status, boolean create);

    @Inject(method = "loadChunk", at = @At("RETURN"))
    private void onLoadChunkFromPacket(int x, int z, BiomeContainer biomes, PacketBuffer buf, CompoundNBT heightmaps, int verticalStripBitmask, CallbackInfoReturnable<Chunk> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            if (ci.getReturnValue() != null) {
                synchronized (LOCK) {
                    UpgradeData upgradeData = ChunkUpgrader.fixChunk(ci.getReturnValue());
                    ((IUpgradableChunk) ci.getReturnValue()).multiconnect_setClientUpgradeData(upgradeData);
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            Chunk chunk = getChunk(x + dx, z + dz, ChunkStatus.FULL, false);
                            if (chunk != null)
                                ((IUpgradableChunk) chunk).multiconnect_onNeighborLoaded();
                        }
                    }
                }
            }
        }
    }

}
