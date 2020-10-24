package net.earthcomputer.multiconnect.protocols.v1_8;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;

public class DataTrackerEntry_1_8 extends DataTracker.Entry<Object> {
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
    public TrackedData<Object> getData() {
        throw new UnsupportedOperationException("Cannot get tracked data for 1.8 tracked data entry");
    }

    @Override
    public DataTracker.Entry<Object> copy() {
        return new DataTrackerEntry_1_8(serializerId, id, get());
    }
}
