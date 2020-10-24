package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.PassiveEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PassiveEntity.class)
public interface PassiveEntityAccessor {
    @Accessor("CHILD")
    static TrackedData<Boolean> getChild() {
        return MixinHelper.fakeInstance();
    }
}
