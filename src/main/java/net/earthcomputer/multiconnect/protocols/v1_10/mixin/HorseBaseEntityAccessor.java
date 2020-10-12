package net.earthcomputer.multiconnect.protocols.v1_10.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.HorseBaseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HorseBaseEntity.class)
public interface HorseBaseEntityAccessor {
    @Accessor("HORSE_FLAGS")
    static TrackedData<Byte> getHorseFlags() {
        return MixinHelper.fakeInstance();
    }
}
