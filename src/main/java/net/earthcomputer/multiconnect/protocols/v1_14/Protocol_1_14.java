package net.earthcomputer.multiconnect.protocols.v1_14;

import net.earthcomputer.multiconnect.protocols.v1_14.mixin.AbstractVillagerAccessor;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.AbstractVillager;

public class Protocol_1_14 extends Protocol_1_14_1 {
    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, EntityDataAccessor<?> data) {
        if (clazz == AbstractVillager.class && data == AbstractVillagerAccessor.getDataUnhappyCounter())
            return false;
        return super.acceptEntityData(clazz, data);
    }
}
