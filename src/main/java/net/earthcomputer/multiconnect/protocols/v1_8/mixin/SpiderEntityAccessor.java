package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.SpiderEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpiderEntity.class)
public interface SpiderEntityAccessor {
    @Accessor("SPIDER_FLAGS")
    static TrackedData<Byte> getSpiderFlags() {
        return MixinHelper.fakeInstance();
    }
}
