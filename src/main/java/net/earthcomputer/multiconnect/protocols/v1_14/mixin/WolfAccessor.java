package net.earthcomputer.multiconnect.protocols.v1_14.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.animal.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Wolf.class)
public interface WolfAccessor {

    @Accessor("DATA_INTERESTED_ID")
    static EntityDataAccessor<Boolean> getDataInterestedId() {
        return MixinHelper.fakeInstance();
    }

}
