package net.earthcomputer.multiconnect.protocols.v1_14;

import net.earthcomputer.multiconnect.impl.DataTrackerManager;
import net.earthcomputer.multiconnect.protocols.v1_14_1.Protocol_1_14_1;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.AbstractTraderEntity;

import java.lang.reflect.Field;

public class Protocol_1_14 extends Protocol_1_14_1 {

    private static final Field HEAD_ROLLING_TIME_LEFT = DataTrackerManager.getTrackedDataField(AbstractTraderEntity.class, 0, "HEAD_ROLLING_TIME_LEFT");

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == AbstractTraderEntity.class && data == DataTrackerManager.getTrackedData(Integer.class, HEAD_ROLLING_TIME_LEFT))
            return false;
        return super.acceptEntityData(clazz, data);
    }
}
