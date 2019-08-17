package net.earthcomputer.multiconnect.protocols.v1_14;

import net.earthcomputer.multiconnect.protocols.v1_14_1.Protocol_1_14_1;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.AbstractTraderEntity;

import java.lang.reflect.Field;
import java.util.Arrays;

public class Protocol_1_14 extends Protocol_1_14_1 {

    private static final Field HEAD_ROLLING_TIME_LEFT;
    static {
        try {
            HEAD_ROLLING_TIME_LEFT = Arrays.stream(AbstractTraderEntity.class.getDeclaredFields())
                    .filter(f -> f.getType() == TrackedData.class)
                    .findFirst().orElseThrow(NoSuchFieldException::new);
            HEAD_ROLLING_TIME_LEFT.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        try {
            if (clazz == AbstractTraderEntity.class && data == HEAD_ROLLING_TIME_LEFT.get(null))
                return false;
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
        return super.acceptEntityData(clazz, data);
    }
}
