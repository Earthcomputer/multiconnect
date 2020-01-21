package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.CatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CatEntity.class)
public interface CatEntityAccessor {
    @Accessor("SLEEPING_WITH_OWNER")
    static TrackedData<Boolean> getSleepingWithOwner() {
        return MixinHelper.fakeInstance();
    }

    @Accessor("HEAD_DOWN")
    static TrackedData<Boolean> getHeadDown() {
        return MixinHelper.fakeInstance();
    }

    @Accessor("COLLAR_COLOR")
    static TrackedData<Boolean> getCollarColor() {
        return MixinHelper.fakeInstance();
    }
}
