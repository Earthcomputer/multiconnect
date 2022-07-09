package net.earthcomputer.multiconnect.protocols.generic;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;

public interface ISynchedEntityData {

    void multiconnect_recomputeItems();

    <T> EntityDataAccessor<T> multiconnect_getActualData(EntityDataAccessor<T> data);

    void multiconnect_setEntityTo(Entity entity);

}
