package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.CreeperEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CreeperEntity.class)
public interface CreeperEntityAccessor {
    @Accessor("CHARGED")
    static TrackedData<Boolean> getCharged() {
        return MixinHelper.fakeInstance();
    }

    @Accessor("IGNITED")
    static TrackedData<Boolean> getIgnited() {
        return MixinHelper.fakeInstance();
    }
}
