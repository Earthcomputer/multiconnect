package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.animal.Rabbit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Rabbit.class)
public interface RabbitAccessor {
    @Accessor("DATA_TYPE_ID")
    static EntityDataAccessor<Integer> getDataTypeId() {
        return MixinHelper.fakeInstance();
    }
}
