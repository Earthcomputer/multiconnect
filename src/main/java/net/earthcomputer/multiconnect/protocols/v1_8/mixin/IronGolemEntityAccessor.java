package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.IronGolemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(IronGolemEntity.class)
public interface IronGolemEntityAccessor {
    @Accessor("IRON_GOLEM_FLAGS")
    static TrackedData<Byte> getIronGolemFlags() {
        return MixinHelper.fakeInstance();
    }
}
