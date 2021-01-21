package net.earthcomputer.multiconnect.protocols.v1_16_4.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.ShulkerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShulkerEntity.class)
public interface ShulkerEntityAccessor {
    @Accessor("PEEK_AMOUNT")
    static TrackedData<Byte> getPeekAmount() {
        return MixinHelper.fakeInstance();
    }
}
