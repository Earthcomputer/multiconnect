package net.earthcomputer.multiconnect.protocols.v1_9_2.mixin;

import net.minecraft.network.packet.s2c.play.ChunkData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ChunkData.class)
public interface ChunkDataAccessor {
    @Accessor
    List<ChunkData.BlockEntityData> getBlockEntities();
}
