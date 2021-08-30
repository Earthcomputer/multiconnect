package net.earthcomputer.multiconnect.mixin.bridge;

import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.BitSet;

@Mixin(ChunkDataS2CPacket.class)
public interface ChunkDataPacketAccessor {
    @Accessor
    @Mutable
    void setData(byte[] data);
    @Accessor
    @Mutable
    void setVerticalStripBitmask(BitSet verticalStripBitmask);
}
