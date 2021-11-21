package net.earthcomputer.multiconnect.mixin.bridge;

import net.minecraft.network.packet.s2c.play.ChunkData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkData.class)
public interface ChunkDataAccessor {
    @Accessor
    @Mutable
    void setSectionsData(byte[] data);
}
