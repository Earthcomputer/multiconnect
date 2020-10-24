package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.ZombieEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ZombieEntity.class)
public interface ZombieEntityAccessor {
    @Accessor("BABY")
    static TrackedData<Boolean> getBaby() {
        return MixinHelper.fakeInstance();
    }
}
