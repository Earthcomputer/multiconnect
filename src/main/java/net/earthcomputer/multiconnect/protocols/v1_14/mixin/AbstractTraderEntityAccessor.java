package net.earthcomputer.multiconnect.protocols.v1_14.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.network.datasync.DataParameter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractVillagerEntity.class)
public interface AbstractTraderEntityAccessor {

    @Accessor("SHAKE_HEAD_TICKS")
    static DataParameter<Integer> getHeadRollingTimeLeft() {
        return MixinHelper.fakeInstance();
    }

}
