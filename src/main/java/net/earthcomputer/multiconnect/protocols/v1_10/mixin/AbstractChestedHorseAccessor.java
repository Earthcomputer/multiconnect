package net.earthcomputer.multiconnect.protocols.v1_10.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractChestedHorse.class)
public interface AbstractChestedHorseAccessor {
    @Accessor("DATA_ID_CHEST")
    static EntityDataAccessor<Boolean> getDataIdChest() {
        return MixinHelper.fakeInstance();
    }
}
