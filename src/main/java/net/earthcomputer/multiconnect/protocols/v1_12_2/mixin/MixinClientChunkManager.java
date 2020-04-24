package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_12_2.ChunkUpgrader;
import net.earthcomputer.multiconnect.protocols.v1_12_2.IUpgradableChunk;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientChunkManager.class)
public abstract class MixinClientChunkManager {

    @Unique private static final Object LOCK = new Object();

    @Shadow public abstract WorldChunk getChunk(int x, int z, ChunkStatus status, boolean create);

    @Inject(method = "loadChunkFromPacket", at = @At("RETURN"))
    private void onLoadChunkFromPacket(int x, int z, BiomeArray biomes, PacketByteBuf buf, CompoundTag heightmaps, int verticalStripBitmask, boolean bl, CallbackInfoReturnable<WorldChunk> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            if (ci.getReturnValue() != null) {
                synchronized (LOCK) {
                    UpgradeData upgradeData = ChunkUpgrader.fixChunk(ci.getReturnValue());
                    ((IUpgradableChunk) ci.getReturnValue()).multiconnect_setClientUpgradeData(upgradeData);
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            WorldChunk chunk = getChunk(x + dx, z + dz, ChunkStatus.FULL, false);
                            if (chunk != null)
                                ((IUpgradableChunk) chunk).multiconnect_onNeighborLoaded();
                        }
                    }
                }
            }
        }
    }

}
