package net.earthcomputer.multiconnect.protocols.v1_10.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractHorse.class)
public interface AbstractHorseAccessor {
    @Accessor("DATA_ID_FLAGS")
    static EntityDataAccessor<Byte> getDataIdFlags() {
        return MixinHelper.fakeInstance();
    }
}
