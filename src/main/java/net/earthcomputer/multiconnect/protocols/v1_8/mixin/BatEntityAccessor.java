package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.BatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BatEntity.class)
public interface BatEntityAccessor {
    @Accessor("BAT_FLAGS")
    static TrackedData<Byte> getBatFlags() {
        return MixinHelper.fakeInstance();
    }
}
