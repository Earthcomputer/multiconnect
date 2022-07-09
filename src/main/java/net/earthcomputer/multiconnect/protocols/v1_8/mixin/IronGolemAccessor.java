package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.animal.IronGolem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(IronGolem.class)
public interface IronGolemAccessor {
    @Accessor("DATA_FLAGS_ID")
    static EntityDataAccessor<Byte> getDataFlagsId() {
        return MixinHelper.fakeInstance();
    }
}
