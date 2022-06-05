package net.earthcomputer.multiconnect.protocols.v1_18_2;

import net.earthcomputer.multiconnect.protocols.generic.DataTrackerManager;
import net.earthcomputer.multiconnect.protocols.v1_18_2.mixin.CatEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_19.Protocol_1_19;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.CatVariant;

public class Protocol_1_18_2 extends Protocol_1_19 {
    public static final TrackedData<Integer> OLD_CAT_VARIANT = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (!super.acceptEntityData(clazz, data)) {
            return false;
        }

        if (clazz == CatEntity.class && data == CatEntityAccessor.getCatVariant()) {
            DataTrackerManager.registerOldTrackedData(CatEntity.class, OLD_CAT_VARIANT, 1, (entity, type) -> {
                entity.setVariant(switch (type) {
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
