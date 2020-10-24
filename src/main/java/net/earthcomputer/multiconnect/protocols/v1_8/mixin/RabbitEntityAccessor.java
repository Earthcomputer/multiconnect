package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.RabbitEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RabbitEntity.class)
public interface RabbitEntityAccessor {
    @Accessor("RABBIT_TYPE")
    static TrackedData<Integer> getRabbitType() {
        return MixinHelper.fakeInstance();
    }
}
