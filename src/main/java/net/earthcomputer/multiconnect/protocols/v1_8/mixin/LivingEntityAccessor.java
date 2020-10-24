package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("HEALTH")
    static TrackedData<Float> getHealth() {
        return MixinHelper.fakeInstance();
    }

    @Accessor("POTION_SWIRLS_COLOR")
    static TrackedData<Integer> getPotionSwirlsColor() {
        return MixinHelper.fakeInstance();
    }

    @Accessor("POTION_SWIRLS_AMBIENT")
    static TrackedData<Boolean> getPotionSwirlsAmbient() {
        return MixinHelper.fakeInstance();
    }
}
