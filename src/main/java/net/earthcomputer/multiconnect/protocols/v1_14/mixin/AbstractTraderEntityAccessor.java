package net.earthcomputer.multiconnect.protocols.v1_14.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.AbstractTraderEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractTraderEntity.class)
public interface AbstractTraderEntityAccessor {

    @Accessor("HEAD_ROLLING_TIME_LEFT")
    static TrackedData<Integer> getHeadRollingTimeLeft() {
        return MixinHelper.fakeInstance();
    }

}
