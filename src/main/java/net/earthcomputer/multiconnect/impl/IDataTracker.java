package net.earthcomputer.multiconnect.impl;

import net.minecraft.entity.data.TrackedData;

public interface IDataTracker {

    void multiconnect_recomputeEntries();

    <T> TrackedData<T> multiconnect_getActualTrackedData(TrackedData<T> data);

}
