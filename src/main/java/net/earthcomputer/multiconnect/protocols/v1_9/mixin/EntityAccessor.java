package net.earthcomputer.multiconnect.protocols.v1_9.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("DATA_NO_GRAVITY")
    static EntityDataAccessor<Boolean> getDataNoGravity() {
        return MixinHelper.fakeInstance();
    }
}
