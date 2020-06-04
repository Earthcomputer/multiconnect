package net.earthcomputer.multiconnect.protocols.v1_15_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.TameableEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TameableEntity.class)
public interface TameableEntityAccessor {
    @Accessor("TAMEABLE_FLAGS")
    static TrackedData<Byte> getTameableFlags() {
        return MixinHelper.fakeInstance();
    }
}
