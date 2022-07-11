package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("DATA_HEALTH_ID")
    static EntityDataAccessor<Float> getDataHealthId() {
        return MixinHelper.fakeInstance();
    }

    @Accessor("DATA_EFFECT_COLOR_ID")
    static EntityDataAccessor<Integer> getDataEffectColorId() {
        return MixinHelper.fakeInstance();
    }

    @Accessor("DATA_EFFECT_AMBIENCE_ID")
    static EntityDataAccessor<Boolean> getDataEffectAmbienceId() {
        return MixinHelper.fakeInstance();
    }
}
