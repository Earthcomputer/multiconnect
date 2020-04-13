package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.network.datasync.DataParameter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MooshroomEntity.class)
public interface MooshroomEntityAccessor {
    @Accessor("MOOSHROOM_TYPE")
    static DataParameter<String> getType() {
        return MixinHelper.fakeInstance();
    }
}
