package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.SlimeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SlimeEntity.class)
public interface SlimeEntityAccessor {
    @Accessor("SLIME_SIZE")
    static TrackedData<Integer> getSlimeSize() {
        return MixinHelper.fakeInstance();
    }
}
