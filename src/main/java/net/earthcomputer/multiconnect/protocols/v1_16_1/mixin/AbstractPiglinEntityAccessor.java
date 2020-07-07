package net.earthcomputer.multiconnect.protocols.v1_16_1.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractPiglinEntity.class)
public interface AbstractPiglinEntityAccessor {
    @Accessor("IMMUNE_TO_ZOMBIFICATION")
    static TrackedData<Boolean> getImmuneToZombification() {
        return MixinHelper.fakeInstance();
    }
}
