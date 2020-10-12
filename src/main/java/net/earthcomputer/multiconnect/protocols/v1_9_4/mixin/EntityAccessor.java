package net.earthcomputer.multiconnect.protocols.v1_9_4.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("NO_GRAVITY")
    static TrackedData<Boolean> getNoGravity() {
        return MixinHelper.fakeInstance();
    }
}
