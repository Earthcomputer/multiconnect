package net.earthcomputer.multiconnect.protocols.generic;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.util.EightWayDirection;
import net.minecraft.world.dimension.DimensionType;

import java.util.BitSet;
import java.util.EnumMap;

public interface IChunkDataS2CPacket {
    boolean multiconnect_isDataTranslated();
    void multiconnect_setDataTranslated(boolean dataTranslated);
    DimensionType multiconnect_getDimension();
    void multiconnect_setDimension(DimensionType dimension);
    void multiconnect_setBlocksNeedingUpdate(EnumMap<EightWayDirection, IntSet> blocksNeedingUpdate);
    EnumMap<EightWayDirection, IntSet> multiconnect_getBlocksNeedingUpdate();

    void setData(byte[] data);
    void setVerticalStripBitmask(BitSet verticalStripBitmask);
}
