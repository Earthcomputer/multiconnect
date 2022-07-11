package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.AgeableMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AgeableMob.class)
public interface AgeableMobAccessor {
    @Accessor("DATA_BABY_ID")
    static EntityDataAccessor<Boolean> getDataBabyId() {
        return MixinHelper.fakeInstance();
    }
}
