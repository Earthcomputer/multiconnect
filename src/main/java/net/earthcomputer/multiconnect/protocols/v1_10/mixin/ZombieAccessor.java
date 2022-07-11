package net.earthcomputer.multiconnect.protocols.v1_10.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Zombie.class)
public interface ZombieAccessor {
    @Accessor("DATA_SPECIAL_TYPE_ID")
    static EntityDataAccessor<Integer> getDataSpecialTypeId() {
        return MixinHelper.fakeInstance();
    }
}
