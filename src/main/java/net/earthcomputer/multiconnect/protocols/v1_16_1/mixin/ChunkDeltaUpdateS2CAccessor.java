package net.earthcomputer.multiconnect.protocols.v1_16_1.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.ChunkSectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkDeltaUpdateS2CPacket.class)
public interface ChunkDeltaUpdateS2CAccessor {
    @Accessor("field_26345")
    void setPos(ChunkSectionPos pos);

    @Accessor("field_26346")
    void setIndices(short[] indices);

    @Accessor("field_26347")
    void setStates(BlockState[] states);
}
