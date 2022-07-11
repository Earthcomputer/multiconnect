package net.earthcomputer.multiconnect.protocols.v1_16;

import net.earthcomputer.multiconnect.protocols.generic.*;
import net.earthcomputer.multiconnect.protocols.v1_16.mixin.AbstractPiglinAccessor;
import net.earthcomputer.multiconnect.protocols.v1_16.mixin.PiglinAccessor;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;

public class Protocol_1_16_1 extends Protocol_1_16_2 {
    private static final EntityDataAccessor<Boolean> OLD_IMMUNE_TO_ZOMBIFICATION = SynchedDataManager.createOldEntityData(EntityDataSerializers.BOOLEAN);

    @Override
    public void preAcceptEntityData(Class<? extends Entity> clazz, EntityDataAccessor<?> data) {
        if (clazz == Piglin.class && data == PiglinAccessor.getDataIsChargingCrossbow()) {
            SynchedDataManager.registerOldEntityData(Piglin.class, OLD_IMMUNE_TO_ZOMBIFICATION, false, AbstractPiglin::setImmuneToZombification);
        }
        super.preAcceptEntityData(clazz, data);
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, EntityDataAccessor<?> data) {
        if (clazz == AbstractPiglin.class && data == AbstractPiglinAccessor.getDataImmuneToZombification()) {
            return false;
        }
        return super.acceptEntityData(clazz, data);
    }
}
