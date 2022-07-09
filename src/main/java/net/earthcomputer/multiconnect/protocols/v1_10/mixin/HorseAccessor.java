package net.earthcomputer.multiconnect.protocols.v1_10.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.animal.horse.Horse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Horse.class)
public interface HorseAccessor {
    @Accessor("DATA_ID_TYPE_VARIANT")
    static EntityDataAccessor<Integer> getDataIdTypeVariant() {
        return MixinHelper.fakeInstance();
    }
}
