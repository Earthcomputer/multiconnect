package net.earthcomputer.multiconnect.protocols.generic;

import net.minecraft.world.dimension.DimensionType;

public interface IChunkDataS2CPacket {
    boolean multiconnect_isDataTranslated();
    void multiconnect_setDataTranslated(boolean dataTranslated);
    DimensionType multiconnect_getDimension();
    void multiconnect_setDimension(DimensionType dimension);

    void setData(byte[] data);
}
