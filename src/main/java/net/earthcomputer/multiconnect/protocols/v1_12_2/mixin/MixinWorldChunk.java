package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.protocols.v1_12_2.IUpgradableChunk;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Chunk.class)
public abstract class MixinWorldChunk implements IUpgradableChunk {

    @Shadow @Final private World world;
    @Shadow @Final private ChunkPos pos;

    @Shadow public abstract ChunkPos getPos();

    @Unique private UpgradeData clientUpgradeData;

    @Override
    public UpgradeData multiconnect_getClientUpgradeData() {
        return clientUpgradeData;
    }

    @Override
    public void multiconnect_setClientUpgradeData(UpgradeData upgradeData) {
        this.clientUpgradeData = upgradeData;
    }

    @Override
    public void multiconnect_onNeighborLoaded() {
        if (clientUpgradeData != null) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (world.getChunk(pos.x + dx, pos.z + dz, ChunkStatus.FULL, false) == null) {
                        return;
                    }
                }
            }
            clientUpgradeData.postProcessChunk((Chunk) (Object) this);
            clientUpgradeData = null;
        }
    }
}
