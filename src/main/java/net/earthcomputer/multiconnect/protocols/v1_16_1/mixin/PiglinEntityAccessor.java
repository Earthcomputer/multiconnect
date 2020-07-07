package net.earthcomputer.multiconnect.protocols.v1_16_1.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.PiglinEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PiglinEntity.class)
public interface PiglinEntityAccessor {
    @Accessor("OLD_IMMUNE_TO_ZOMBIFICATION")
    static TrackedData<Boolean> getOldImmuneToZombification() {
        return MixinHelper.fakeInstance();
    }
}
