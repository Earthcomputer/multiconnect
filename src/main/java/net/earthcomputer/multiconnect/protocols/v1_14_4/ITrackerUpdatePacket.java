package net.earthcomputer.multiconnect.protocols.v1_14_4;

import net.minecraft.entity.data.DataTracker;

import java.util.List;

public interface ITrackerUpdatePacket {

    void setId(int id);

    void setTrackedValues(List<DataTracker.Entry<?>> entries);

}
