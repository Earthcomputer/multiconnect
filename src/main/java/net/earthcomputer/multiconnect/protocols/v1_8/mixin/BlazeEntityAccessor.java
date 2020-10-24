package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.BlazeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlazeEntity.class)
public interface BlazeEntityAccessor {
    @Accessor("BLAZE_FLAGS")
    static TrackedData<Byte> getBlazeFlags() {
        return MixinHelper.fakeInstance();
    }
}
