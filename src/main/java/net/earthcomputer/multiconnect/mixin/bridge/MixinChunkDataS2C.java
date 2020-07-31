package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.protocols.generic.IChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkDataS2CPacket.class)
public abstract class MixinChunkDataS2C implements IChunkDataS2CPacket {
    @Unique
    private boolean dataTranslated = false;
    @Unique
    private DimensionType dimension;

    @Override
    public boolean multiconnect_isDataTranslated() {
        return dataTranslated;
    }

    @Override
    public void multiconnect_setDataTranslated(boolean dataTranslated) {
        this.dataTranslated = dataTranslated;
    }

    @Override
    public DimensionType multiconnect_getDimension() {
        return dimension;
    }

    @Override
    public void multiconnect_setDimension(DimensionType dimension) {
        this.dimension = dimension;
    }

    @Override
    @Accessor
    public abstract void setData(byte[] data);
}
