package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Creeper.class)
public interface CreeperAccessor {
    @Accessor("DATA_IS_POWERED")
    static EntityDataAccessor<Boolean> getDataIsPowered() {
        return MixinHelper.fakeInstance();
    }

    @Accessor("DATA_IS_IGNITED")
    static EntityDataAccessor<Boolean> getDataIsIgnited() {
        return MixinHelper.fakeInstance();
    }
}
