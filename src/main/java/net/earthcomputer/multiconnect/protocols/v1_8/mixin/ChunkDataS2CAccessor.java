package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ChunkDataS2CPacket.class)
public interface ChunkDataS2CAccessor {
    @Accessor
    void setChunkX(int chunkX);

    @Accessor
    void setChunkZ(int chunkZ);

    @Accessor
    void setVerticalStripBitmask(int verticalStripBitmask);

    @Accessor
    void setHeightmaps(CompoundTag heightmaps);

    @Accessor
    void setBiomeArray(int[] biomeArray);

    @Accessor
    void setBlockEntities(List<CompoundTag> blockEntities);

    @Accessor
    void setIsFullChunk(boolean isFullChunk);
}
