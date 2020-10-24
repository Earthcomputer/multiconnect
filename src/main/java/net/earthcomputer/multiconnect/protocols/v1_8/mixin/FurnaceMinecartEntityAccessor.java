package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FurnaceMinecartEntity.class)
public interface FurnaceMinecartEntityAccessor {
    @Accessor("LIT")
    static TrackedData<Boolean> getLit() {
        return MixinHelper.fakeInstance();
    }
}
