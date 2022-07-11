package net.earthcomputer.multiconnect.protocols.v1_11.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.animal.Pig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Pig.class)
public interface PigAccessor {
    @Accessor("DATA_BOOST_TIME")
    static EntityDataAccessor<Integer> getDataBoostTime() {
        return MixinHelper.fakeInstance();
    }
}
