package net.earthcomputer.multiconnect.impl;

import net.minecraft.network.datasync.DataParameter;

public interface IDataTracker {

    void multiconnect_recomputeEntries();

    <T> DataParameter<T> multiconnect_getActualDataParameter(DataParameter<T> data);

}
