package net.earthcomputer.multiconnect.protocols.v1_14;

import net.earthcomputer.multiconnect.protocols.v1_14.mixin.MerchantEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_14_1.Protocol_1_14_1;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.MerchantEntity;

public class Protocol_1_14 extends Protocol_1_14_1 {
    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == MerchantEntity.class && data == MerchantEntityAccessor.getHeadRollingTimeLeft())
            return false;
        return super.acceptEntityData(clazz, data);
    }
}
