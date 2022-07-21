package net.earthcomputer.multiconnect.protocols.v1_13.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Zombie.class)
public interface ZombieAccessor {
    @Accessor("DATA_DROWNED_CONVERSION_ID")
    static EntityDataAccessor<Boolean> getDataDrownedConversionId() {
        return MixinHelper.fakeInstance();
    }
}
