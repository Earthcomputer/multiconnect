package net.earthcomputer.multiconnect.protocols.v1_15.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.animal.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Wolf.class)
public interface WolfAccessor {
    @Accessor("DATA_REMAINING_ANGER_TIME")
    static EntityDataAccessor<Integer> getDataRemainingAngerTime() {
        return MixinHelper.fakeInstance();
    }
}
