package net.earthcomputer.multiconnect.protocols.v1_18;

import net.earthcomputer.multiconnect.protocols.generic.SynchedDataManager;
import net.earthcomputer.multiconnect.protocols.v1_18.mixin.CatAccessor;
import net.earthcomputer.multiconnect.protocols.v1_19.Protocol_1_19;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.CatVariant;

public class Protocol_1_18_2 extends Protocol_1_19 {
    public static final EntityDataAccessor<Integer> OLD_CAT_VARIANT = SynchedDataManager.createOldEntityData(EntityDataSerializers.INT);

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, EntityDataAccessor<?> data) {
        if (!super.acceptEntityData(clazz, data)) {
            return false;
        }

        if (clazz == Cat.class && data == CatAccessor.getDataVariantId()) {
            SynchedDataManager.registerOldEntityData(Cat.class, OLD_CAT_VARIANT, 1, (entity, type) -> {
                entity.setCatVariant(switch (type) {
                    case 0 -> CatVariant.TABBY;
                    case 2 -> CatVariant.RED;
                    case 3 -> CatVariant.SIAMESE;
                    case 4 -> CatVariant.BRITISH_SHORTHAIR;
                    case 5 -> CatVariant.CALICO;
                    case 6 -> CatVariant.PERSIAN;
                    case 7 -> CatVariant.RAGDOLL;
                    case 8 -> CatVariant.WHITE;
                    case 9 -> CatVariant.JELLIE;
                    case 10 -> CatVariant.ALL_BLACK;
                    default -> CatVariant.BLACK;
                });
            });
            return false;
        }

        return true;
    }
}
