package net.earthcomputer.multiconnect.protocols.v1_14;

import net.earthcomputer.multiconnect.protocols.v1_14.mixin.AbstractTraderEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_14_1.Protocol_1_14_1;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.network.datasync.DataParameter;

public class Protocol_1_14 extends Protocol_1_14_1 {

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, DataParameter<?> data) {
        if (clazz == AbstractVillagerEntity.class && data == AbstractTraderEntityAccessor.getHeadRollingTimeLeft())
            return false;
        return super.acceptEntityData(clazz, data);
    }
}
