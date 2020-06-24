package net.earthcomputer.multiconnect.protocols.generic;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;

public interface IDataTracker {

    void multiconnect_recomputeEntries();

    <T> TrackedData<T> multiconnect_getActualTrackedData(TrackedData<T> data);

    void multiconnect_setEntityTo(Entity entity);

}
