package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.EndermanEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EndermanEntity.class)
public interface EndermanEntityAccessor {
    @Accessor("ANGRY")
    static TrackedData<Boolean> getAngry() {
        return MixinHelper.fakeInstance();
    }
}
