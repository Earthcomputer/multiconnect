package net.earthcomputer.multiconnect.protocols.v1_14_4.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.datasync.DataParameter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {

    @Accessor("BEE_STING_COUNT")
    static DataParameter<Integer> getStingerCount() {
        return MixinHelper.fakeInstance();
    }

}
