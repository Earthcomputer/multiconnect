package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.network.datasync.DataParameter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CatEntity.class)
public interface CatEntityAccessor {
    @Accessor("field_213428_bG")
    static DataParameter<Boolean> getSleepingWithOwner() {
        return MixinHelper.fakeInstance();
    }

    @Accessor("field_213429_bH")
    static DataParameter<Boolean> getHeadDown() {
        return MixinHelper.fakeInstance();
    }

    @Accessor("COLLAR_COLOR")
    static DataParameter<Boolean> getCollarColor() {
        return MixinHelper.fakeInstance();
    }
}
