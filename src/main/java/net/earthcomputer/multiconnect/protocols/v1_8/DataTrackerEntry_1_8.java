package net.earthcomputer.multiconnect.protocols.v1_8;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;

public class DataTrackerEntry_1_8 extends SynchedEntityData.DataItem<Object> {
    private final int serializerId;
    private final int id;

    public DataTrackerEntry_1_8(int serializerId, int id, Object value) {
        super(null, value);
        this.serializerId = serializerId;
        this.id = id;
    }

    public int getSerializerId() {
        return serializerId;
    }

    public int getId() {
        return id;
    }

    @Override
    public EntityDataAccessor<Object> getAccessor() {
        throw new UnsupportedOperationException("Cannot get tracked data for 1.8 tracked data entry");
    }

    @Override
    public SynchedEntityData.DataItem<Object> copy() {
        return new DataTrackerEntry_1_8(serializerId, id, getValue());
    }
}
