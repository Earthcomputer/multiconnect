package net.earthcomputer.multiconnect.protocols.v1_13.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.animal.Cat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Cat.class)
public interface CatAccessor {
    @Accessor("IS_LYING")
    static EntityDataAccessor<Boolean> getIsLying() {
        return MixinHelper.fakeInstance();
    }

    @Accessor("RELAX_STATE_ONE")
    static EntityDataAccessor<Boolean> getRelaxStateOne() {
        return MixinHelper.fakeInstance();
    }

    @Accessor("DATA_COLLAR_COLOR")
    static EntityDataAccessor<Boolean> getDataCollarColor() {
        return MixinHelper.fakeInstance();
    }
}
